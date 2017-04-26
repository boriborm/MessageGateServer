package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.dao.PhoneGrantDAO;
import com.bankir.mgs.hibernate.model.PhoneGrant;
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

@Path("/phonegrants")
public class PhoneGrants extends BaseServlet{
    private static final Logger logger = LoggerFactory.getLogger(PhoneGrants.class);
    private static final String[] viewPhoneGrantsRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};
    private static final String[] editPhoneGrantsRoles = {User.ROLE_EDITOR};

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public MgsJsonObject create(PhoneGrant phoneGrant) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(editPhoneGrantsRoles);

        MgsJsonObject json;
        StatelessSession session = null;
        // Открываем сессию с транзакцией

        try {

            session = Config.getHibernateSessionFactory().openStatelessSession();
            session.getTransaction().begin();

            //Сохраняем данные в БД
            PhoneGrantDAO pgDao = new PhoneGrantDAO(session);
            PhoneGrant pg = pgDao.getById(phoneGrant.getPhoneNumber());

            if (pg!=null){
                pgDao.save(phoneGrant);
            } else {
                pgDao.add(phoneGrant);
            }

            session.getTransaction().commit();
            json = MgsJsonObject.Success();

        }catch (JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            if (session!=null) try {session.getTransaction().rollback();} catch (Exception ignored){}
            json = new MgsJsonObject("Ошибка сохранения: " + e.getSQLException().getMessage());

        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }

        return json;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public MgsJsonObject list(
            @QueryParam("query") String q
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewPhoneGrantsRoles);

        //Если фильтр не задан, возвращаем пустой список
        if (q==null||q.length()==0) return new MgsJsonObject(true);
        StatelessSession session = null;

        MgsJsonObject json;

        try {


            String hqlFrom = " FROM PhoneGrant pg";
            String hqlWhere = " where pg.phoneNumber like :query";

            List<FilterProperty> filterProperties = new ArrayList<>();
            filterProperties.add(new FilterProperty("query", q+"%"));

            session = Config.getHibernateSessionFactory().openStatelessSession();
            //Ограничние на возврат первых 30 записей, подходящих по маске.
            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, 0, 30, filterProperties, null)
                    .setReadOnly(true);

            json = new MgsJsonObject(query.list());

        }catch(Exception e){
            logger.error("Error: "+e.getMessage(), e);
            json = new MgsJsonObject(e.getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }
        return json;
    }

}