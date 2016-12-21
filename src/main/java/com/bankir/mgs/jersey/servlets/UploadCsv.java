package com.bankir.mgs.jersey.servlets;

import javax.ws.rs.Path;

@Path("/files")
public class UploadCsv {
/*
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
*/
}
