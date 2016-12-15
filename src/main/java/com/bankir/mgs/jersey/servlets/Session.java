package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Authorization;
import com.bankir.mgs.Config;
import com.bankir.mgs.User;
import com.bankir.mgs.jersey.PasswordStorage;
import com.bankir.mgs.jersey.model.JsonObject;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/session")
public class Session extends BaseServlet{

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public JsonObject login(@FormParam("login") String login, @FormParam("password") String password){

        HttpSession httpSession = request.getSession();
        httpSession.removeAttribute(HttpHeaders.AUTHORIZATION);

        User user;
        try {
            user = Authorization.Authorize(login, password);
        } catch (PasswordStorage.InvalidHashException e) {
            throw new WebApplicationException(
                    JsonObject.getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        } catch (PasswordStorage.CannotPerformOperationException e) {
            throw new WebApplicationException(
                    JsonObject.getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
        httpSession.setAttribute("user", user);

        if (user.isAnonymous()){
            throw new WebApplicationException(
                    JsonObject.getErrorResponse(Response.Status.UNAUTHORIZED, Config.MSG_FORBIDDEN)
            );
        }

        return successJsonObject;
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public JsonObject logout(){
        HttpSession httpSession = request.getSession();
        httpSession.invalidate();
        return new JsonObject(true);
    }

    @GET
    @Path("/getuser")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public JsonObject getUser(){
        HttpSession httpSession = request.getSession();
        return new JsonObject(httpSession.getAttribute("user"));
    }
}
