package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.hibernate.dao.UserMessageTypeDAO;
import com.bankir.mgs.hibernate.model.UserMessageType;
import com.bankir.mgs.jersey.model.MgsJsonObject;
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
    public MgsJsonObject create(UserMessageTypeObject userMessageType) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject result;

        // Открываем сессию с транзакцией
        StatelessSession session = null;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            session.getTransaction().begin();

            //Сохраняем данные сценария в БД
            UserMessageTypeDAO dao = new UserMessageTypeDAO(session);
            UserMessageType umt = new UserMessageType(
                    userMessageType.getTypeId(),
                    userMessageType.getUserId()
            );
            dao.add(umt);

            session.getTransaction().commit();
            result = new MgsJsonObject(userMessageType);

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){};
            result = new MgsJsonObject("Ошибка: " + e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }


        return result;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public MgsJsonObject delete(UserMessageTypeObject userMessageType, @PathParam("id") String id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject result;

        // Открываем сессию с транзакцией
        StatelessSession session = null;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            session.getTransaction().begin();

            UserMessageTypeDAO dao = new UserMessageTypeDAO(session);
            UserMessageType umt = new UserMessageType(
                    userMessageType.getTypeId(),
                    userMessageType.getUserId()
            );
            dao.delete(umt);

            session.getTransaction().commit();
            result = MgsJsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){};
            result = new MgsJsonObject("Ошибка удаления типа сообщения в БД: " + e.getSQLException());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }
        return result;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public MgsJsonObject list(
            @QueryParam("userId") Long userId
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        StatelessSession session = null;
        MgsJsonObject result;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();

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

            result = new MgsJsonObject(userMessageTypes);
            result.setTotal(((Integer) userMessageTypes.size()).longValue());

        }catch(Exception e){
            logger.error("Error: "+e.getMessage(), e);
            result = new MgsJsonObject(e.getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }
        return result;
    }

}