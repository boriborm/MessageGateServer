package com.bankir.mgs.jersey;

import com.bankir.mgs.Config;
import com.google.gson.GsonBuilder;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AccessDeniedHandler implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException weException) {

        // get initial response
        Response response = weException.getResponse();
        MediaType mt = response.getMediaType();
        GsonBuilder gb = new GsonBuilder();
        System.out.println(gb.setPrettyPrinting().create().toJson(response));
        // create custom error
        String error = Config.MSG_FORBIDDEN;

        // return the custom error
        return Response.status(response.getStatus()).entity(error).build();
    }
}