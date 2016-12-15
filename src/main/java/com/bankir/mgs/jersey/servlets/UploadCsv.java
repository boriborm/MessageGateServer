package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.jersey.model.JsonObject;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/files")
public class UploadCsv {

    @POST
    @Path("/pdf")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public JsonObject uploadPdfFile(
            @FormDataParam("charset") String charset,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
    {
        System.out.println("Charset "+charset);
        JsonObject jResp;

        try
        {


            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, charset), 1);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            bufferedReader.close();
            jResp = new JsonObject(true);
        } catch (IOException e)
        {
            jResp = new JsonObject(e.getMessage());
        }
        return jResp;
    }

}
