package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Authorization;
import com.bankir.mgs.Config;
import com.bankir.mgs.User;
import com.bankir.mgs.jersey.PasswordStorage;
import com.bankir.mgs.jersey.model.MgsJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/session")
public class Session extends BaseServlet{
    private static final Logger logger = LoggerFactory.getLogger(Session.class);
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public MgsJsonObject login(@FormParam("login") String login, @FormParam("password") String password){

        HttpSession httpSession = request.getSession();
        httpSession.removeAttribute(HttpHeaders.AUTHORIZATION);

        MgsJsonObject result;
        User user;
        try {
            user = Authorization.Authorize(login, password);
            result = MgsJsonObject.Success();
        } catch (PasswordStorage.InvalidHashException e) {
            logger.error("Error: "+e.getMessage(), e);
            throw new WebApplicationException(
                    MgsJsonObject.getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        } catch (PasswordStorage.CannotPerformOperationException e) {
            logger.error("Error: "+e.getMessage(), e);
            throw new WebApplicationException(
                    MgsJsonObject.getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
        httpSession.setAttribute("user", user);

        if (user.isAnonymous()){
            throw new WebApplicationException(
                    MgsJsonObject.getErrorResponse(Response.Status.UNAUTHORIZED, Config.MSG_FORBIDDEN)
            );
        }

        logger.debug("Session id: {}, user \"{}\" login.", httpSession.getId(), user.getLogin());
        return result;
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public MgsJsonObject logout(){
        HttpSession httpSession = request.getSession();
        User user = (User) httpSession.getAttribute("user");
        logger.debug("Session id: {}, user \"{}\" logout.", httpSession.getId(), user.getLogin());
        httpSession.invalidate();
        return MgsJsonObject.Success();
    }

    @GET
    @Path("/getuser")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public MgsJsonObject getUser(){
        HttpSession httpSession = request.getSession();
        return new MgsJsonObject(httpSession.getAttribute("user"));
    }
}
