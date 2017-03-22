package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.*;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.model.Message;
import com.bankir.mgs.jersey.model.JsonObject;
import com.bankir.mgs.jersey.model.MessageCreationRequestObject;
import com.bankir.mgs.jersey.model.MessageCreationResponseObject;
import com.bankir.mgs.jersey.model.MessageObject;
import org.hibernate.JDBCException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/messages")
public class Messages extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(Messages.class);
    private final static String[] viewMessagesRoles = {User.ROLE_READER};
    private final static String[] createMessagesRoles = {User.ROLE_RESTSERVICE, User.ROLE_SENDER};

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object create(MessageCreationRequestObject data){

        authorizeOrThrow(createMessagesRoles);

        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        Object response;
        try {

            MessageGenerator mg = new MessageGenerator(user, session, null);
            MessageCreationResponseObject resp = mg.generate(data);

            //Сигнализируем процессу обработки очереди о необходимости начать обработку
            if (resp.isSuccess()) {
                if (resp.getSuccessMessages().size() > 0) {
                    QueueProcessor.getInstance().signal();
                }
            }
            response =  resp;

        } catch (Exception e) {
            logger.error("Error: "+e.getMessage(), e);
            response = new JsonObject(e.getMessage());
        }

        session.close();

        if (user.userWithRole(User.ROLE_RESTSERVICE)) request.getSession().invalidate();

        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject list(
            @DefaultValue("0") @QueryParam("start") int start,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("1") @QueryParam("page") int page,
            @QueryParam("sort") String sort,
            @QueryParam("filter") String filter
    ) {
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewMessagesRoles);

        List<FilterProperty> filterProperties = Utils.parseFilterProperties(filter);


        setDatePropertyTime(filterProperties, "beginDate", new Date(), 0,0,0);
        setDatePropertyTime(filterProperties, "endDate", new Date(), 23,59,59);



        String hqlFrom = " FROM Message m";

        String hqlJoin = " LEFT JOIN User u ON m.userId=u.id"
                       + " LEFT JOIN MessageType mt ON m.typeId=mt.typeId"
                       + " LEFT JOIN Scenario s ON m.scenarioId=s.id";

        String hqlWhere = " WHERE m.createDate BETWEEN :beginDate and :endDate";

        FilterProperty propertyBulkId = Utils.getFilterProperty(filterProperties,"bulkId");
        if (propertyBulkId!=null){
            Long bulkId = propertyBulkId.getLongValue();
            if (bulkId != null&&bulkId>0) {
                hqlJoin+=" JOIN BulkMessage bm ON bm.messageId=m.id";
                hqlWhere+=" and bm.bulkId=:bulkId";
            }
        }

        FilterProperty propertyTypeId = Utils.getFilterProperty(filterProperties,"typeId");
        if (propertyTypeId!=null){
            String typeId = propertyTypeId.getStringValue();
            if (typeId!=null && typeId.length()>0) {
                hqlWhere+=" and mt.typeId=:typeId";
            }
        }

        FilterProperty propertyReportStatusGroup = Utils.getFilterProperty(filterProperties,"statusGroupName");
        if (propertyReportStatusGroup!=null){
            String groupName = propertyReportStatusGroup.getStringValue();
            if (groupName != null&&groupName.length()>0) {
                hqlJoin+=" JOIN Report rp ON rp.messageId=m.id";
                hqlWhere+=" and rp.groupName=:statusGroupName";
            }
        }

        String hql = hqlFrom + hqlJoin + hqlWhere;

        List<SorterProperty> sorterProperties = Utils.parseSortProperties(sort);

        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        JsonObject json;
        try {


            String countQ = "Select count (m.id) " + hql;
            Query countQuery = Utils.createQuery(session, countQ, null, null, filterProperties, null);
            Long countResults = (Long) countQuery.uniqueResult();

            Query query = Utils.createQuery(session, hql, start, limit, filterProperties, sorterProperties)
                    .setReadOnly(true);

            com.bankir.mgs.hibernate.model.User usr;
            com.bankir.mgs.hibernate.model.Scenario scenario;
            com.bankir.mgs.hibernate.model.MessageType msgType;

            ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);

            List<MessageObject> messages = new ArrayList<>();
            while (results.next()) {

                Message msg = (Message) results.get(0);
                usr = (com.bankir.mgs.hibernate.model.User) results.get(1);
                msgType = (com.bankir.mgs.hibernate.model.MessageType) results.get(2);
                scenario = (com.bankir.mgs.hibernate.model.Scenario) results.get(3);

                MessageObject message = new MessageObject();
                message.setId(msg.getId());
                message.setPhoneNumber(msg.getPhoneNumber());
                message.setCreateDate(msg.getCreateDate());
                message.setExternalId(msg.getExternalId());
                message.setSmsText(msg.getSmsText());
                message.setViberText(msg.getViberText());
                message.setVoiceText(msg.getVoiceText());
                message.setParsecoText(msg.getParsecoText());
                message.setUser(
                    new MessageObject.User(
                        usr.getId(), usr.getUserName()
                    )
                );
                message.setMessageType(
                    new MessageObject.MessageType(
                        msgType.getTypeId(),
                        msgType.getDescription(),
                        msgType.isAcceptSms(),
                        msgType.isAcceptViber(),
                        msgType.isAcceptVoice(),
                        msgType.isAcceptParseco()
                    )
                );
                message.setScenario(
                    new MessageObject.Scenario(
                        scenario.getId(),
                        scenario.getScenarioName(),
                        scenario.getScenarioKey()
                    )
                );

                messages.add(message);
            }
            results.close();

            json = new JsonObject(messages);
            json.setTotal(countResults);



        }catch(JDBCException e){
            logger.error("Error: "+e.getSQLException().getMessage(), e);
            json = new JsonObject(e.getSQLException().getMessage());
        }

        session.close();
        return json;
    }

    private static void setDatePropertyTime(List<FilterProperty> filterProperties, String propertyName, Date defaultDate, int h, int m, int s){
        FilterProperty propertyDate = Utils.getFilterProperty(filterProperties, propertyName);
        if (propertyDate!=null){
            Date date = propertyDate.getDateValue();
            propertyDate.setValue(dateWithTime((date==null?defaultDate:date), h,m,s));
        } else {
            filterProperties.add(new FilterProperty(propertyName, dateWithTime(defaultDate,h,m,s)));
        }

    }
}
