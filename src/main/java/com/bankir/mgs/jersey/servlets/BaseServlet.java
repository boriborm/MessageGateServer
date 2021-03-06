package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.User;
import com.bankir.mgs.jersey.model.MgsJsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Calendar;
import java.util.Date;

abstract class BaseServlet {

    @Context
    SecurityContext securityContext;

    @Context
    HttpServletRequest request;

    User user = null;

    static final String[] adminRoles = {User.ROLE_ADMIN};

    void authorizeOrThrow(String[] roles){
        this.user = (User) securityContext.getUserPrincipal();

        String token = request.getHeader("User-Token");

        if ( !user.getUserToken().equals(token) &&
             !user.userWithRole(User.ROLE_RESTSERVICE)
           ){
            throw new WebApplicationException(
                    MgsJsonObject.getErrorResponse(Response.Status.FORBIDDEN, Config.MSG_FORBIDDEN)
            );
        }

        boolean hasRole = false;
        for (String role : roles){
            if (this.user.userWithRole(role)) {
                hasRole = true;
                break;
            }
        }
        if (!hasRole){
            throw new WebApplicationException(
                    MgsJsonObject.getErrorResponse(Response.Status.FORBIDDEN, Config.MSG_FORBIDDEN)
            );
        }

    }

/*
    static Date dateFromString(String dateInString) throws ParseException {
        //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return formatter.parse(dateInString);
    }
*/
    static void throwException(String errorMessage) {
        throw new WebApplicationException(
                MgsJsonObject.getErrorResponse(Response.Status.OK, errorMessage)
        );
    }

    static Date dateWithTime(Date date, int hours, int minutes, int seconds)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        return cal.getTime();
    }

}
