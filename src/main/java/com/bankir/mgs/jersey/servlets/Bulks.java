package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.jersey.model.MgsJsonObject;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/bulks")
public class Bulks extends BaseServlet{
    private static final Logger logger = LoggerFactory.getLogger(Bulks.class);
    private static final String[] viewBulksRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public MgsJsonObject list(
            @QueryParam("query") String q
    ){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewBulksRoles);

        //Если фильтр не задан, возвращаем пустой список
        if (q==null||q.length()==0) return new MgsJsonObject(true);

        StatelessSession session = null;
        MgsJsonObject json;
        try {


            String hqlFrom = " FROM Bulk b";
            String hqlWhere = " where upper(b.description) like upper(:query)";

            List<FilterProperty> filterProperties = new ArrayList<>();
            filterProperties.add(new FilterProperty("query", "%"+q+"%"));

            session = Config.getHibernateSessionFactory().openStatelessSession();
            //Ограничние на возврат первых 30 записей, подходящих по маске.
            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, 0, 30, filterProperties, null)
                    .setReadOnly(true);

            json = new MgsJsonObject(query.list());

        }catch(Exception e){
            logger.error("Error: "+e.getMessage());
            json = new MgsJsonObject(e.getMessage());
        } finally {
            if (session!=null) try {session.close();} catch (Exception ignored){}
        }
        return json;
    }

}