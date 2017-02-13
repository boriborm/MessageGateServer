package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.SorterProperty;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.dao.MessageTypeDAO;
import com.bankir.mgs.hibernate.dao.UserDAO;
import com.bankir.mgs.hibernate.dao.UserMessageTypeDAO;
import com.bankir.mgs.hibernate.model.MessageType;
import com.bankir.mgs.hibernate.model.User;
import com.bankir.mgs.hibernate.model.UserMessageType;
import com.bankir.mgs.jersey.PasswordStorage;
import com.bankir.mgs.jersey.model.JsonObject;
import com.bankir.mgs.jersey.model.UserObject;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/users")
public class Users extends BaseServlet{
    private static final Logger logger = LoggerFactory.getLogger(Users.class);
    @POST
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonObject create(UserObject user) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные сценария в БД
        UserDAO dao = new UserDAO(session);
        try {

            //Проверяем наличие пользователей с таким логином
            User usr = dao.getByLogin(user.getLogin());
            if (usr!=null){
                session.close();
                throwException("Пользователь с логином \""+user.getLogin()+"\" уже существует.");
            }

            //рассчитываем хэш информацию по паролю
            String hashInfo = null;
            try {
                hashInfo = PasswordStorage.createHash(user.getPassword().toCharArray());
            } catch (PasswordStorage.CannotPerformOperationException e) {
                session.close();
                throwException(e.getMessage());
            }

            usr = new User(
                    user.getLogin(),
                    hashInfo,
                    false,
                    user.getUserName()
            );
            //переносим роли
            usr.setRoles(user.getRoles());
            dao.add(usr);

            //заполняем для возврата полученный идентификатор и обнуляем пароль, чтобы не светился лишний раз
            user.setId(usr.getId());
            user.setPassword(null);

            //Добавляме доступ к типу сообщения по умолчанию

            MessageTypeDAO mtdao = new MessageTypeDAO(session);
            MessageType defaultMt = mtdao.getById(Config.getSettings().getDefaultMessageType());
            if (defaultMt != null){
                UserMessageTypeDAO umtdao = new UserMessageTypeDAO(session);
                UserMessageType umt = new UserMessageType(defaultMt.getTypeId(), usr.getId());
                umtdao.add(umt);
            }


            session.getTransaction().commit();
            json = new JsonObject(user);

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка создания типа сообщения в БД: " + e.getSQLException().getMessage());

        }

        // Закрываем сессию
        session.close();

        return json;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public JsonObject update(UserObject user, @PathParam("id") Long id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные сценария в БД
        UserDAO dao = new UserDAO(session);
        try {

            //Проверяем наличие пользователей с таким логином
            User usr = dao.getById(id);
            if (usr==null){
                session.close();
                throwException("Пользователь с идентификатором "+id+" не найден.");
            }

            usr.setLocked(user.isLocked());
            usr.setUserName(user.getUserName());
            usr.setRoles(user.getRoles());

            dao.save(usr);

            session.getTransaction().commit();
            json = JsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка обновления данных пользователя в БД: " + e.getSQLException().getMessage());
        }

        // Закрываем сессию
        session.close();

        return json;

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/passwupd")
    public JsonObject updatePassword(UserObject user) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные сценария в БД
        UserDAO dao = new UserDAO(session);
        try {

            //Проверяем наличие пользователей с таким логином
            User usr = dao.getByLogin(user.getLogin());
            if (usr==null){
                session.close();
                throwException("Пользователь с логином \""+user.getLogin()+"\" не найден.");
            }

            //рассчитываем хэш информацию по паролю
            String hashInfo = null;
            try {
                hashInfo = PasswordStorage.createHash(user.getPassword().toCharArray());
            } catch (PasswordStorage.CannotPerformOperationException e) {
                session.close();
                throwException(e.getMessage());
            }

            usr.setHashInfo(hashInfo);

            dao.save(usr);

            session.getTransaction().commit();
            json = JsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка обновления пароля пользователя в БД: " + e.getSQLException().getMessage());
        }

        // Закрываем сессию
        session.close();

        return json;

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public JsonObject delete(UserObject user, @PathParam("id") Long id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        //Проверяем наличие сообщений с таким типом

        List<FilterProperty> filterProperties = new ArrayList<>();
        filterProperties.add(new FilterProperty("userId", id));
        Query query = Utils.createQuery(session,  "From Message where userId=:userId", 0, 1, filterProperties, null)
                .setReadOnly(true);
        List result = query.list();
        System.out.println(result.size());
        if (result.size()>0){
            throwException("Удаление невозможно. Идентификатор пользователя использован в сообщениях!");
        }

        session.getTransaction().begin();
        //Сохраняем данные сценария в БД
        UserDAO dao = new UserDAO(session);
        try {
            User usr = new User(id);
            dao.delete(usr);

            session.getTransaction().commit();
            json = JsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка удаления типа сообщения в БД: " + e.getSQLException().getMessage());
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
            @DefaultValue("ALL") @QueryParam("locked") String active
    ){

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        JsonObject json;

        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        try {


            List<FilterProperty> filterProperties = Utils.parseFilterProperties(filter);
            List<SorterProperty> sorterProperties = Utils.parseSortProperties(sort);

            String hqlFrom = " FROM User u";
            String hqlWhere = "";
            if ("Y".equalsIgnoreCase(active)){
                hqlWhere = " where u.locked=true";
            }
            if ("N".equalsIgnoreCase(active)){
                hqlWhere = " where u.locked=false";
            }

            String countQ = "SELECT count (u.id) " + hqlFrom + hqlWhere;
            Query countQuery = Utils.createQuery(session, countQ, null, null, filterProperties, null);
            Long countResults = (Long) countQuery.uniqueResult();

            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, start, limit, filterProperties, sorterProperties)
                    .setReadOnly(true);

            List<UserObject> users = new ArrayList<>();
            List hibernateUsers =  query.list();
            for (Object hibernateUser:hibernateUsers){
                User usr = (User) hibernateUser;
                users.add(
                        new UserObject(
                                usr.getId(),
                                usr.getLogin(),
                                usr.getUserName(),
                                usr.getRoles(),
                                usr.isLocked()
                        )
                );
            }
            json = new JsonObject(users);
            json.setTotal(countResults);

        }catch(JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            json = new JsonObject(e.getSQLException().getMessage());
        }

        session.close();
        return json;

    }

}