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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


@Path("/messagetypes")
public class MessageTypes extends BaseServlet{

    private static final String[] viewMessageTypesRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};

    @POST
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonObject create(MessageTypeObject messageType) {

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
                    messageType.getTypeId(),
                    messageType.getDescription(),
                    messageType.isAcceptSms(),
                    messageType.isAcceptViber(),
                    messageType.isAcceptVoice(),
                    messageType.isAcceptParseco(),
                    messageType.getSmsValidityPeriod(),
                    messageType.getViberValidityPeriod(),
                    messageType.getParsecoValidityPeriod(),
                    messageType.getVoiceValidityPeriod(),
                    messageType.isActive()
            );
            dao.add(mt);

            session.getTransaction().commit();
            json = new JsonObject(messageType);

        }catch (JDBCException e){
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
    public JsonObject update(MessageTypeObject messageType, @PathParam("id") String id) {

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

            mt.setDescription(messageType.getDescription());
            mt.setAcceptSms(messageType.isAcceptSms());
            mt.setAcceptViber(messageType.isAcceptViber());
            mt.setAcceptVoice(messageType.isAcceptVoice());
            mt.setAcceptParseco(messageType.isAcceptParseco());
            mt.setSmsValidityPeriod(messageType.getSmsValidityPeriod());
            mt.setViberValidityPeriod(messageType.getViberValidityPeriod());
            mt.setParsecoValidityPeriod(messageType.getParsecoValidityPeriod());
            mt.setVoiceValidityPeriod(messageType.getVoiceValidityPeriod());
            mt.setActive(messageType.isActive());

            dao.save(mt);

            session.getTransaction().commit();
            json = successJsonObject;

        }catch (JDBCException e){
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
    public JsonObject delete(MessageTypeObject messageType, @PathParam("id") String id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        //Проверяем наличие сообщений с таким типом

        List<FilterProperty> filterProperties = new ArrayList<>();
        filterProperties.add(new FilterProperty("typeId", messageType.getTypeId()));
        Query query = Utils.createQuery(session,  "From Message where typeId=:typeId", 0, 1, filterProperties, null)
                .setReadOnly(true);
        List result = query.list();
        System.out.println(result.size());
        if (result.size()>0){
            throwException("Удаление невозможно. Тип сообщения \""+messageType.getTypeId()+"\" использован в сообщениях!");
        }

        session.getTransaction().begin();
        //Сохраняем данные сценария в БД
        MessageTypeDAO dao = new MessageTypeDAO(session);
        try {
            MessageType mt = new MessageType(id);
            dao.delete(mt);
            session.getTransaction().commit();
            json = successJsonObject;

        }catch (JDBCException e){
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
            String hqlWhere = "";
            if ("Y".equalsIgnoreCase(active)){
                hqlWhere = " where mt.active=true";
            }
            if ("N".equalsIgnoreCase(active)){
                hqlWhere = " where mt.active=false";
            }

            String countQ = "SELECT count (mt.typeId) " + hqlFrom + hqlWhere;
            Query countQuery = Utils.createQuery(session, countQ, null, null, filterProperties, null);
            Long countResults = (Long) countQuery.uniqueResult();

            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, start, limit, filterProperties, sorterProperties)
                    .setReadOnly(true);


            List<MessageTypeObject> messageTypes = new ArrayList<>();

            List hibernateMessageTypes =  query.list();
            for (Object hibernateMessageType:hibernateMessageTypes){
                com.bankir.mgs.hibernate.model.MessageType mt = (com.bankir.mgs.hibernate.model.MessageType) hibernateMessageType;
                messageTypes.add(
                        new MessageTypeObject(
                                mt.getTypeId(),
                                mt.getDescription(),
                                mt.isAcceptSms(),
                                mt.isAcceptViber(),
                                mt.isAcceptParseco(),
                                mt.isAcceptVoice(),
                                mt.getSmsValidityPeriod(),
                                mt.getViberValidityPeriod(),
                                mt.getParsecoValidityPeriod(),
                                mt.getVoiceValidityPeriod(),
                                mt.isActive()
                        )
                );
            }

            JsonObject json = new JsonObject(messageTypes);
            json.setTotal(countResults);

            session.close();


            return json;
        }catch(Exception e){
            return new JsonObject(e.getMessage());
        }
    }

}