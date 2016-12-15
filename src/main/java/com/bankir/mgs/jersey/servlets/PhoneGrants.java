package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.dao.PhoneGrantDAO;
import com.bankir.mgs.hibernate.model.PhoneGrant;
import com.bankir.mgs.jersey.model.JsonObject;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/phonegrants")
public class PhoneGrants extends BaseServlet{

    private static final String[] viewPhoneGrantsRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};
    private static final String[] editPhoneGrantsRoles = {User.ROLE_EDITOR};

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonObject create(PhoneGrant phoneGrant) {

        /* Авторизация пользователя по роли */
        authorizeOrThrow(editPhoneGrantsRoles);

        JsonObject json;

        // Открываем сессию с транзакцией
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        session.getTransaction().begin();

        //Сохраняем данные в БД
        PhoneGrantDAO pgDao = new PhoneGrantDAO(session);
        try {

            PhoneGrant pg = pgDao.getById(phoneGrant.getPhoneNumber());

            if (pg!=null){
                pgDao.save(phoneGrant);
            } else {
                pgDao.add(phoneGrant);
            }

            session.getTransaction().commit();
            json = new JsonObject(true);

        }catch (JDBCException e){
            session.getTransaction().rollback();
            json = new JsonObject("Ошибка сохранения: " + e.getSQLException());

        }

        // Закрываем сессию
        session.close();

        return json;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject list(
            @QueryParam("query") String q
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewPhoneGrantsRoles);

        //Если фильтр не задан, возвращаем пустой список
        if (q==null||q.length()==0) return new JsonObject(true);

        try {
            StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

            String hqlFrom = " FROM PhoneGrant pg";
            String hqlWhere = " where pg.phoneNumber like :query";

            List<FilterProperty> filterProperties = new ArrayList<>();
            filterProperties.add(new FilterProperty("query", q+"%"));

            //Ограничние на возврат первых 30 записей, подходящих по маске.
            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, 0, 30, filterProperties, null)
                    .setReadOnly(true);

            JsonObject json = new JsonObject(query.list());

            session.close();


            return json;
        }catch(Exception e){
            return new JsonObject(e.getMessage());
        }
    }

}