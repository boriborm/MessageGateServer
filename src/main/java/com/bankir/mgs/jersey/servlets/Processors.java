package com.bankir.mgs.jersey.servlets;


import com.bankir.mgs.FileProcessor;
import com.bankir.mgs.jersey.model.JsonObject;
import com.bankir.mgs.jersey.model.ProcessorsStatusObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/processors")
public class Processors extends BaseServlet{

    @GET
    @Path("/{process}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Object process(@PathParam("process") String process) {

         /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        com.bankir.mgs.QueueProcessor qp = com.bankir.mgs.QueueProcessor.getInstance();
        com.bankir.mgs.DeliveryReportProcessor drp = com.bankir.mgs.DeliveryReportProcessor.getInstance();
        FileProcessor fp = FileProcessor.getInstance();

        if ("startQueue".equalsIgnoreCase(process)) qp.startProcessor();
        if ("stopQueue".equalsIgnoreCase(process)) qp.stopProcessor();
        if ("startDelivery".equalsIgnoreCase(process)) drp.startProcessor();
        if ("stopDelivery".equalsIgnoreCase(process)) drp.stopProcessor();

        if ("startFile".equalsIgnoreCase(process)) fp.startProcessor();
        if ("stopFile".equalsIgnoreCase(process)) fp.stopProcessor();

        if ("status".equalsIgnoreCase(process)){
            ProcessorsStatusObject resp = new ProcessorsStatusObject();
            resp.setQueue(new ProcessorsStatusObject.Status(qp.isActive(), qp.getSleepTime(), qp.getStatus()));
            resp.setDelivery(new ProcessorsStatusObject.Status(drp.isActive(), drp.getSleepTime(), drp.getStatus()));
            resp.setFile(new ProcessorsStatusObject.Status(fp.isActive(), fp.getSleepTime(), fp.getStatus()));
            return resp;
        }
        return JsonObject.Success();
    }
}