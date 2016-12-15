package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.SorterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.model.Report;
import com.bankir.mgs.jersey.model.JsonObject;
import com.bankir.mgs.jersey.model.ReportObject;
import org.hibernate.JDBCException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервлет обрабатывает ссылки:
 * ../reports            GET     admin   получение списка типов сообщений
 */

@Path("/reports")
public class Reports extends BaseServlet{

    private static final String[] viewReportsRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_EDITOR};

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject list(
            @QueryParam("start") int start,
            @QueryParam("limit") int limit,
            @QueryParam("page") int page,
            @QueryParam("sort") String sort,
            @QueryParam("filter") String filter,
            @QueryParam("messageId") Long messageId
    ){

        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewReportsRoles);
        System.out.println("messageid "+messageId);
        if (messageId== null) {
            JsonObject json = new JsonObject(true);
            json.setTotal(0L);
            return json;
        }

        try {
            StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

            List<FilterProperty> filterProperties = Utils.parseFilterProperties(filter);
            List<SorterProperty> sorterProperties = Utils.parseSortProperties(sort);

            String hqlFrom = " FROM Report r";
            String hqlWhere = " WHERE r.messageId = :messageId";
            filterProperties.add(new FilterProperty("messageId", messageId));

            String countQ = "SELECT count (r.id) " + hqlFrom + hqlWhere;
            Query countQuery = Utils.createQuery(session, countQ, null, null, filterProperties, null);
            Long countResults = (Long) countQuery.uniqueResult();

            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, start, limit, filterProperties, sorterProperties)
                    .setReadOnly(true);
            ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);

            List<ReportObject> reports = new ArrayList<>();

            while (results.next()) {
                Report report = (Report) results.get(0);
                reports.add(
                    new ReportObject(
                            report.getId(),
                            report.getMessageId(),
                            report.getSentAt(),
                            report.getDoneAt(),
                            report.getChannel(),
                            report.getMessageCount(),
                            report.getPricePerMessage(),
                            report.getPriceCurrency(),
                            report.getReportDate(),
                            report.getStatusName(),
                            report.getStatusGroupName(),
                            report.getStatusDescription()
                    )
                );
            }
            results.close();

            JsonObject json = new JsonObject(reports);
            json.setTotal(countResults);

            session.close();


            return json;
        }catch(JDBCException e){
            return new JsonObject(e.getSQLException());
        }
    }

}