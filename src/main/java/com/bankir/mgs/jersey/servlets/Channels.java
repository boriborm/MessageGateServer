package com.bankir.mgs.jersey.servlets;

import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.Utils;
import com.bankir.mgs.hibernate.model.MessageType;
import com.bankir.mgs.jersey.model.ChannelObject;
import com.bankir.mgs.jersey.model.JsonObject;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


@Path("/channels")
public class Channels extends BaseServlet{
    private static final Logger logger = LoggerFactory.getLogger(Channels.class);
    private static final String[] viewChannelsRoles = {User.ROLE_ADMIN, User.ROLE_READER, User.ROLE_SENDER};

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public JsonObject list(){
        /* Авторизация пользователя по роли */
        authorizeOrThrow(viewChannelsRoles);

        try {
            StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

            List<FilterProperty> filterProperties = new ArrayList<>();

            String hqlFrom = " FROM MessageType mt";
            String hqlWhere = " where 1=1";

            // Если пользователь не с административной ролью, то ограничиваем доступ к типам сообщений по пользователю
            if (!user.userWithRole(User.ROLE_ADMIN)){
                hqlWhere += " and exists(From UserMessageType umt where umt.userId=:userId and umt.typeId=mt.typeId";
                filterProperties.add(new FilterProperty("userId", user.getId()));
            }

            Query query = Utils.createQuery(session,  hqlFrom + hqlWhere, 0, 1000, filterProperties, null)
                    .setReadOnly(true);



            List hibernateMessageTypes =  query.list();
            String channels = "";
            for (Object hibernateMessageType:hibernateMessageTypes){
                MessageType mt = (MessageType) hibernateMessageType;
                if (mt.isAcceptSms() && !channels.contains("S")) channels +="S";
                if (mt.isAcceptViber() && !channels.contains("V")) channels +="V";
                if (mt.isAcceptParseco() && !channels.contains("P")) channels +="P";
                if (mt.isAcceptVoice() && !channels.contains("O")) channels +="O";
                if (mt.isAcceptFacebook() && !channels.contains("F")) channels +="F";
            }

            List<ChannelObject> channelObjects = new ArrayList<>();

            if (channels.contains("S")) channelObjects.add(new ChannelObject("S", "SMS"));
            if (channels.contains("V")) channelObjects.add(new ChannelObject("V", "VIBER"));
            if (channels.contains("P")) channelObjects.add(new ChannelObject("P", "PARSECO"));
            if (channels.contains("O")) channelObjects.add(new ChannelObject("O", "VOICE"));
            if (channels.contains("F")) channelObjects.add(new ChannelObject("F", "FACEBOOK"));

            JsonObject json = new JsonObject(channelObjects);
            json.setTotal((long) channels.length());

            session.close();


            return json;
        }catch(Exception e){
            logger.error("Error: "+e.getMessage(), e);
            return new JsonObject(e.getMessage());
        }
    }
}