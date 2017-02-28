package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.hibernate.dao.UserMessageTypeDAO;
import com.bankir.mgs.hibernate.model.UserMessageType;
import com.bankir.mgs.jersey.model.JsonObject;
import com.bankir.mgs.jersey.model.UserMessageTypeObject;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


@Path("/usermessagetypes")
public class UserMessageTypes extends BaseServlet{
    private static final Logger logger = LoggerFactory.getLogger(UserMessageTypes.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonObject create(UserMessageTypeObject userMessageType) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные сценария в БД
        UserMessageTypeDAO dao = new UserMessageTypeDAO(session);
        try {

            UserMessageType umt = new UserMessageType(
                    userMessageType.getTypeId(),
                    userMessageType.getUserId()
            );
            dao.add(umt);

            session.getTransaction().commit();
            json = new JsonObject(userMessageType);

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка: " + e.getSQLException().getMessage());

        }

        session.close();
        return json;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public JsonObject delete(UserMessageTypeObject userMessageType, @PathParam("id") String id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        UserMessageTypeDAO dao = new UserMessageTypeDAO(session);
        try {
            UserMessageType umt = new UserMessageType(
                    userMessageType.getTypeId(),
                    userMessageType.getUserId()
            );
            dao.delete(umt);

            session.getTransaction().commit();
            json = JsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка удаления типа сообщения в БД: " + e.getSQLException());
        }

        session.close();
        return json;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject list(
            @QueryParam("userId") Long userId
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        JsonObject json;
        try {


            UserMessageTypeDAO dao = new UserMessageTypeDAO(session);
            List<UserMessageTypeObject> userMessageTypes = new ArrayList<>();
            for (UserMessageType userMessageType:dao.getByUserId(userId)){
                userMessageTypes.add(
                        new UserMessageTypeObject(
                                userMessageType.getTypeId(),
                                userMessageType.getUserId()
                        )
                );
            }

            json = new JsonObject(userMessageTypes);
            json.setTotal(((Integer) userMessageTypes.size()).longValue());

        }catch(Exception e){
            logger.error("Error: "+e.getMessage(), e);
            json = new JsonObject(e.getMessage());
        }
        session.close();
        return json;
    }

}