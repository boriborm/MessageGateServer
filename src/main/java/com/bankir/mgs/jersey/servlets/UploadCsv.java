package com.bankir.mgs.jersey.servlets;

import javax.ws.rs.Path;

@Path("/files")
public class UploadCsv {
/*
    @POST
    @Path("/pdf")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public MgsJsonObject uploadPdfFile(
            @FormDataParam("charset") String charset,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
    {
        System.out.println("Charset "+charset);
        MgsJsonObject jResp;

        try
        {


            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, charset), 1);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            bufferedReader.close();
            jResp = new MgsJsonObject(true);
        } catch (IOException e)
        {
            jResp = new MgsJsonObject(e.getMessage());
        }
        return jResp;
    }
*/
}
