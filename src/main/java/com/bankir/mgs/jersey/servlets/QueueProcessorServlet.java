package com.bankir.mgs.jersey.servlets;

/**
 * Created by bankir on 14.10.16.
 */

import com.bankir.mgs.QueueProcessor;
import com.bankir.mgs.jersey.model.JsonObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/queueprocessor")
public class QueueProcessorServlet extends BaseServlet{

    @GET
    @Path("start")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject start() {

         /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        QueueProcessor qp = QueueProcessor.getInstance();
        qp.startProcessor();
        return successJsonObject;
    }

    @GET
    @Path("stop")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject stop() {

         /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        QueueProcessor qp = QueueProcessor.getInstance();
        qp.stopProcessor();
        return successJsonObject;
    }


}