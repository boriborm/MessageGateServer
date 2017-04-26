package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.SorterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.dao.MessageTypeDAO;
import com.bankir.mgs.hibernate.model.MessageType;
import com.bankir.mgs.jersey.model.MessageTypeObject;
import com.bankir.mgs.jersey.model.MgsJsonObject;
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
    public MgsJsonObject create(MessageTypeObject messageTypeObject) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject json;
        StatelessSession session = null;

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

            session = Config.getHibernateSessionFactory().openStatelessSession();
            session.getTransaction().begin();

            //Сохраняем данные сценария в БД
            MessageTypeDAO dao = new MessageTypeDAO(session);

            dao.add(mt);

            session.getTransaction().commit();
            json = new MgsJsonObject(messageTypeObject);

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject("Ошибка создания типа сообщения в БД: " + e.getSQLException());

        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return json;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public MgsJsonObject update(MessageTypeObject messageTypeObject, @PathParam("id") String id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject json;

        StatelessSession session = null;

        try {
// Открываем сессию с транзакцией

            session = Config.getHibernateSessionFactory().openStatelessSession();


            //Сохраняем данные сценария в БД
            MessageTypeDAO dao = new MessageTypeDAO(session);
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

            session.getTransaction().begin();
            dao.save(mt);

            session.getTransaction().commit();
            json = MgsJsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject("Ошибка обновления типа сообщения в БД: " + e.getSQLException());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return json;

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public MgsJsonObject delete(MessageTypeObject messageTypeObject, @PathParam("id") String id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        List<FilterProperty> filterProperties = new ArrayList<>();
        filterProperties.add(new FilterProperty("typeId", messageTypeObject.getTypeId()));

        try {
            //Проверяем наличие сообщений с таким типом

            session = Config.getHibernateSessionFactory().openStatelessSession();
            Query query = Utils.createQuery(session,  "From Message where typeId=:typeId", 0, 1, filterProperties, null)
                    .setReadOnly(true);
            List result = query.list();
            System.out.println(result.size());
            if (result.size()==0){
                session.getTransaction().begin();
                //Сохраняем данные сценария в БД
                MessageTypeDAO dao = new MessageTypeDAO(session);

                MessageType mt = new MessageType(id);
                dao.delete(mt);
                session.getTransaction().commit();
                json = MgsJsonObject.Success();
            } else
                json = new MgsJsonObject("Удаление невозможно. Тип сообщения \""+messageTypeObject.getTypeId()+"\" использован в сообщениях!");

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject("Ошибка удаления типа сообщения в БД: " + e.getSQLException());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return json;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public MgsJsonObject list(
            @QueryParam("start") int start,
            @QueryParam("limit") int limit,
            @QueryParam("page") int page,
            @QueryParam("sort") String sort,
            @QueryParam("filter") String filter,
            @DefaultValue("ALL") @QueryParam("active") String active
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewMessageTypesRoles);
        StatelessSession session = null;
        MgsJsonObject json;
        try {


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
            session = Config.getHibernateSessionFactory().openStatelessSession();
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

            json = new MgsJsonObject(messageTypeObjects);
            json.setTotal(countResults);

        }catch(Exception e){
            logger.error("Error: "+e.getMessage(), e);
            json = new MgsJsonObject(e.getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }
        return json;
    }
}