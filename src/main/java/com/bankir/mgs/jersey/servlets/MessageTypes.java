package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.SorterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.dao.MessageTypeDAO;
import com.bankir.mgs.hibernate.model.MessageType;
import com.bankir.mgs.jersey.model.JsonObject;
import com.bankir.mgs.jersey.model.MessageTypeObject;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


@Path("/messagetypes")
public class MessageTypes extends BaseServlet{
    private static final Logger logger = LoggerFactory.getLogger(MessageTypes.class);
    private static final String[] viewMessageTypesRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};

    @POST
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonObject create(MessageTypeObject messageTypeObject) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные сценария в БД
        MessageTypeDAO dao = new MessageTypeDAO(session);
        try {

            MessageType mt = new MessageType(
                    messageTypeObject.getTypeId(),
                    messageTypeObject.getDescription(),
                    messageTypeObject.isAcceptSms(),
                    messageTypeObject.isAcceptViber(),
                    messageTypeObject.isAcceptVoice(),
                    messageTypeObject.isAcceptParseco(),
                    messageTypeObject.isAcceptFacebook(),
                    messageTypeObject.getSmsValidityPeriod(),
                    messageTypeObject.getViberValidityPeriod(),
                    messageTypeObject.getParsecoValidityPeriod(),
                    messageTypeObject.getVoiceValidityPeriod(),
                    messageTypeObject.getFacebookValidityPeriod(),
                    messageTypeObject.isActive(),
                    messageTypeObject.isVerifyImsi()
            );
            dao.add(mt);

            session.getTransaction().commit();
            json = new JsonObject(messageTypeObject);

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка создания типа сообщения в БД: " + e.getSQLException());

        }

        // Закрываем сессию
        session.close();

        return json;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public JsonObject update(MessageTypeObject messageTypeObject, @PathParam("id") String id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные сценария в БД
        MessageTypeDAO dao = new MessageTypeDAO(session);
        try {

            MessageType mt = dao.getById(id);

            mt.setDescription(messageTypeObject.getDescription());
            mt.setAcceptSms(messageTypeObject.isAcceptSms());
            mt.setAcceptViber(messageTypeObject.isAcceptViber());
            mt.setAcceptVoice(messageTypeObject.isAcceptVoice());
            mt.setAcceptParseco(messageTypeObject.isAcceptParseco());
            mt.setAcceptFacebook(messageTypeObject.isAcceptFacebook());
            mt.setSmsValidityPeriod(messageTypeObject.getSmsValidityPeriod());
            mt.setViberValidityPeriod(messageTypeObject.getViberValidityPeriod());
            mt.setParsecoValidityPeriod(messageTypeObject.getParsecoValidityPeriod());
            mt.setVoiceValidityPeriod(messageTypeObject.getVoiceValidityPeriod());
            mt.setFacebookValidityPeriod(messageTypeObject.getFacebookValidityPeriod());
            mt.setActive(messageTypeObject.isActive());
            mt.setVerifyImsi(messageTypeObject.isVerifyImsi());

            dao.save(mt);

            session.getTransaction().commit();
            json = JsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка обновления типа сообщения в БД: " + e.getSQLException());
        }

        // Закрываем сессию
        session.close();

        return json;

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public JsonObject delete(MessageTypeObject messageTypeObject, @PathParam("id") String id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        //Проверяем наличие сообщений с таким типом

        List<FilterProperty> filterProperties = new ArrayList<>();
        filterProperties.add(new FilterProperty("typeId", messageTypeObject.getTypeId()));
        Query query = Utils.createQuery(session,  "From Message where typeId=:typeId", 0, 1, filterProperties, null)
                .setReadOnly(true);
        List result = query.list();
        System.out.println(result.size());
        if (result.size()>0){
            throwException("Удаление невозможно. Тип сообщения \""+messageTypeObject.getTypeId()+"\" использован в сообщениях!");
        }

        session.getTransaction().begin();
        //Сохраняем данные сценария в БД
        MessageTypeDAO dao = new MessageTypeDAO(session);
        try {
            MessageType mt = new MessageType(id);
            dao.delete(mt);
            session.getTransaction().commit();
            json = JsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка удаления типа сообщения в БД: " + e.getSQLException());
        }

        // Закрываем сессию
        session.close();

        return json;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject list(
            @QueryParam("start") int start,
            @QueryParam("limit") int limit,
            @QueryParam("page") int page,
            @QueryParam("sort") String sort,
            @QueryParam("filter") String filter,
            @DefaultValue("ALL") @QueryParam("active") String active
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewMessageTypesRoles);

        try {
            StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

            List<FilterProperty> filterProperties = Utils.parseFilterProperties(filter);
            List<SorterProperty> sorterProperties = Utils.parseSortProperties(sort);

            String hqlFrom = " FROM MessageType mt";
            String hqlWhere = " where 1=1";

            if ("Y".equalsIgnoreCase(active)){
                hqlWhere += " and mt.active=true";
            }
            if ("N".equalsIgnoreCase(active)){
                hqlWhere += " and mt.active=false";
            }

            // Если пользователь не с административной ролью, то ограничиваем доступ к типам сообщений по пользователю
            if (!user.userWithRole(User.ROLE_ADMIN)){
                hqlWhere += " and exists(From UserMessageType umt where umt.userId=:userId and umt.typeId=mt.typeId";
                filterProperties.add(new FilterProperty("userId", user.getId()));
            }

            String countQ = "SELECT count (mt.typeId) " + hqlFrom + hqlWhere;
            Query countQuery = Utils.createQuery(session, countQ, null, null, filterProperties, null);
            Long countResults = (Long) countQuery.uniqueResult();

            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, start, limit, filterProperties, sorterProperties)
                    .setReadOnly(true);


            List<MessageTypeObject> messageTypeObjects = new ArrayList<>();

            List hibernateMessageTypes =  query.list();
            for (Object hibernateMessageType:hibernateMessageTypes){
                com.bankir.mgs.hibernate.model.MessageType mt = (com.bankir.mgs.hibernate.model.MessageType) hibernateMessageType;
                messageTypeObjects.add(
                        new MessageTypeObject(
                                mt.getTypeId(),
                                mt.getDescription(),
                                mt.isAcceptSms(),
                                mt.isAcceptViber(),
                                mt.isAcceptParseco(),
                                mt.isAcceptVoice(),
                                mt.isAcceptFacebook(),
                                mt.getSmsValidityPeriod(),
                                mt.getViberValidityPeriod(),
                                mt.getParsecoValidityPeriod(),
                                mt.getVoiceValidityPeriod(),
                                mt.getFacebookValidityPeriod(),
                                mt.isActive(),
                                mt.isVerifyImsi()
                        )
                );
            }

            JsonObject json = new JsonObject(messageTypeObjects);
            json.setTotal(countResults);

            session.close();


            return json;
        }catch(Exception e){
            logger.error("Error: "+e.getMessage(), e);
            return new JsonObject(e.getMessage());
        }
    }
}