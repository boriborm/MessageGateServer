package com.bankir.mgs.jersey;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class GsonJerseyProvider implements MessageBodyWriter<Object>, MessageBodyReader<Object> {
    private static final String UTF_8 = "UTF-8";

    private Gson gson;

    //Customize the gson behavior here
    private Gson getGson() {
        if (gson == null) {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gson = gsonBuilder.disableHtmlEscaping()
                    .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                    .setPrettyPrinting()
                    //.serializeNulls()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
        }
        return gson;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
                              java.lang.annotation.Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType,
                           Annotation[] annotations, MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(entityStream, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            Type jsonType;
            if (type.equals(genericType)) {
                jsonType = type;
            } else {
                jsonType = genericType;
            }
            return getGson().fromJson(streamReader, jsonType);
        } finally {
            try {
                streamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(Object object, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException,
            WebApplicationException {
        OutputStreamWriter writer = new OutputStreamWriter(entityStream, UTF_8);
        try {
            Type jsonType;
            if (type.equals(genericType)) {
                jsonType = type;
            } else {
                jsonType = genericType;
            }
            getGson().toJson(object, jsonType, writer);
        } finally {
            writer.close();
        }
    }
}