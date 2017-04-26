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
import com.bankir.mgs.jersey.model.MgsJsonObject;
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
    public MgsJsonObject create(UserObject user) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject result;

        // Открываем сессию с транзакцией
        StatelessSession session = null;
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();


            //Сохраняем данные сценария в БД
            UserDAO dao = new UserDAO(session);
            //Проверяем наличие пользователей с таким логином
            User usr = dao.getByLogin(user.getLogin());
            if (usr==null) {
                //рассчитываем хэш информацию по паролю
                String hashInfo = null;

                hashInfo = PasswordStorage.createHash(user.getPassword().toCharArray());

                usr = new User(
                        user.getLogin(),
                        hashInfo,
                        false,
                        user.getUserName()
                );
                //переносим роли
                usr.setRoles(user.getRoles());

                session.getTransaction().begin();

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
                result = new MgsJsonObject(user);
            } else
                result = new MgsJsonObject("Пользователь с логином \""+user.getLogin()+"\" уже существует.");

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){};
            result = new MgsJsonObject("Ошибка создания типа сообщения в БД: " + e.getSQLException().getMessage());
        } catch (PasswordStorage.CannotPerformOperationException e) {
            result = new MgsJsonObject(e.getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return result;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public MgsJsonObject update(UserObject user, @PathParam("id") Long id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = null;
        try {

            session = Config.getHibernateSessionFactory().openStatelessSession();
            //Сохраняем данные сценария в БД
            UserDAO dao = new UserDAO(session);

            //Проверяем наличие пользователей с таким логином
            User usr = dao.getById(id);
            if (usr!=null) {
                usr.setLocked(user.isLocked());
                usr.setUserName(user.getUserName());
                usr.setRoles(user.getRoles());

                session.getTransaction().begin();

                dao.save(usr);

                session.getTransaction().commit();
                json = MgsJsonObject.Success();

            } else
                json = new MgsJsonObject("Пользователь с идентификатором "+id+" не найден.");

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject("Ошибка обновления данных пользователя в БД: " + e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return json;

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/passwupd")
    public MgsJsonObject updatePassword(UserObject user) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();


        //Сохраняем данные сценария в БД
        UserDAO dao = new UserDAO(session);
        try {

            //Проверяем наличие пользователей с таким логином
            User usr = dao.getByLogin(user.getLogin());
            if (usr!=null){
                //рассчитываем хэш информацию по паролю
                String hashInfo = PasswordStorage.createHash(user.getPassword().toCharArray());

                usr.setHashInfo(hashInfo);

                session.getTransaction().begin();

                dao.save(usr);

                session.getTransaction().commit();
                json = MgsJsonObject.Success();
            } else
                json = new MgsJsonObject("Пользователь с логином \""+user.getLogin()+"\" не найден.");

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject("Ошибка обновления пароля пользователя в БД: " + e.getSQLException().getMessage());
        } catch (PasswordStorage.CannotPerformOperationException e) {
            json = new MgsJsonObject(e.getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return json;

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public MgsJsonObject delete(UserObject user, @PathParam("id") Long id) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject json;

        // Открываем сессию с транзакцией

        StatelessSession session = null;
        //Проверяем наличие сообщений с таким типом

        List<FilterProperty> filterProperties = new ArrayList<>();
        filterProperties.add(new FilterProperty("userId", id));
        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            Query query = Utils.createQuery(session,  "From Message where userId=:userId", 0, 1, filterProperties, null)
                    .setReadOnly(true);
            List result = query.list();

            if (result.size()==0) {
                User usr = new User(id);

                session.getTransaction().begin();
                //Сохраняем данные сценария в БД
                UserDAO dao = new UserDAO(session);

                dao.delete(usr);

                session.getTransaction().commit();
                json = MgsJsonObject.Success();

            } else
                json = new MgsJsonObject("Удаление невозможно. Идентификатор пользователя использован в сообщениях!");

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject("Ошибка удаления типа сообщения в БД: " + e.getSQLException().getMessage());
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
            @DefaultValue("ALL") @QueryParam("locked") String active
    ){

        /* Авторизация пользователя по роли */
        authorizeOrThrow(adminRoles);

        MgsJsonObject json;

        StatelessSession session = null;

        try {

            session = Config.getHibernateSessionFactory().openStatelessSession();

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
            json = new MgsJsonObject(users);
            json.setTotal(countResults);

        }catch(JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject(e.getSQLException().getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return json;

    }

}