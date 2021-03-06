package com.bankir.mgs;

import com.bankir.mgs.hibernate.AcceptChannels;
import com.bankir.mgs.hibernate.dao.*;
import com.bankir.mgs.hibernate.model.*;
import com.bankir.mgs.jersey.model.MessageCreationRequestObject;
import com.bankir.mgs.jersey.model.MessageCreationResponseObject;
import org.hibernate.JDBCException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MessageGenerator {

    public static final int MAX_MESSAGES=1000;

    private static final Logger logger = LoggerFactory.getLogger(MessageGenerator.class);


    private StatelessSession session;
    private Long bulkId;
    private long userId;
    private String userLogin;
    private String infobipLogin;
    private HashMap<String, Long> scCache = new HashMap<>();
    private String msgPrefix;

    public MessageGenerator(User user, StatelessSession session, Long bulkId) {

        this.userId = user.getId();
        this.userLogin = user.getLogin();
        this.session = session;
        this.bulkId = bulkId;
        this.infobipLogin = Config.getSettings().getInfobip().getLogin();

        msgPrefix = Config.getSettings().getMessageIdPrefix();
        if (msgPrefix!=null&&msgPrefix.length()>0) msgPrefix = msgPrefix+"-";
        else msgPrefix = "";
    }

    public MessageCreationResponseObject generate(MessageCreationRequestObject data) {

        /* Проверим количество сообщений */
        if (data.getMessages().size()>MAX_MESSAGES){
            return new MessageCreationResponseObject("Количество сообщений не должно превышать "+MAX_MESSAGES);
        }

        List<MessageCreationResponseObject.SuccessMessage> successMessages = new ArrayList<>();
        List<MessageCreationResponseObject.FailedMessage> failedMessages = new ArrayList<>();

        //StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();


        /*
        String scenarioKey;

        if (data.getChannels()==null) {

        } else{
            scenarioDAO.getByChannels()
        }

        String scenarioKey = data.getScenarioKey();
        if (scenarioKey==null) scenarioKey = Config.getSettings().getDefaultScenarioKey();

        Scenario scenario = scenarioDAO.getByKey(scenarioKey, infobipLogin);
        if ( scenario == null ){
            return new MessageCreationResponseObject("Сценарий не найден");
        }

        Long baseScenarioId = scenario.getId();
*/
        String baseChannels = data.getChannels();

        //Проверка правильности типа сообщения
        MessageTypeDAO msgTypeDAO = new MessageTypeDAO(session);
        String messageType = data.getMessageType();
        if (messageType==null) messageType = Config.getSettings().getDefaultMessageType();

        MessageType msgType = msgTypeDAO.getById(messageType);

        if (msgType == null) {
            return new MessageCreationResponseObject("Тип сообщения "+messageType+" не найден");
        }

        if (!msgType.isActive()) {
            return new MessageCreationResponseObject("Тип сообщения "+messageType+" не активен");
        }

        UserMessageTypeDAO userMessageTypeDAO = new UserMessageTypeDAO(session);
        UserMessageType umt = userMessageTypeDAO.get(messageType, userId);
        if (umt == null){
            return new MessageCreationResponseObject("У пользователя " + userLogin + " нет доступа к типу сообщения " + messageType);
        }

        /* Убираем из списка каналов те, который не разрешены типом сообщения */
        baseChannels = channelsAnd(baseChannels, msgType);

        if (baseChannels.length()==0) {
            return new MessageCreationResponseObject("Отсутствуют доступные каналы для рассылки");
        }

        /* Определяем подходящий сценарий по оставшимся типам сообщения */
        ScenarioDAO scenarioDAO = new ScenarioDAO(session);
        Scenario scenario = scenarioDAO.getByChannels(baseChannels, infobipLogin);
        if ( scenario == null ){
            return new MessageCreationResponseObject("Подходящий сценарий на каналы " + baseChannels + " не найден");
        }

        long baseScenarioId = scenario.getId();

        //Список телефонов для выборки из PhoneGrant
        List<String> listPhones = new ArrayList<>();

        for (Iterator<MessageCreationRequestObject.Message> iterMessage = data.getMessages().listIterator(); iterMessage.hasNext(); ) {

            MessageCreationRequestObject.Message message = iterMessage.next();

            /* Проверка правильности номеров телефонов (длина 11 символов) */
            String phone = message.getPhoneNumber();
            if ((phone == null) || (!phone.matches("^\\d{11}$"))) {
                failedMessages.add(
                        new MessageCreationResponseObject.FailedMessage(
                                message.getMessageId(),
                                "Некорректный номер телефона"
                        )
                );
                iterMessage.remove();
                continue;
            }

            /* Добавляем в список телефонов для отправки */
            listPhones.add(phone);
        }


        HashMap<String, PhoneGrant> phgCache = new HashMap<>();
        /* Кэшируем гранты на телефоны */
        if(listPhones.size()>0) {
            Query phoneGrantsQuery = session.createQuery("From PhoneGrant pg where pg.phoneNumber in (:listPhones)")
                    .setParameter("listPhones", listPhones);
            ScrollableResults results = phoneGrantsQuery.scroll(ScrollMode.FORWARD_ONLY);
            while (results.next()) {
                PhoneGrant pg = (PhoneGrant) results.get(0);
                //Добавляем в кэш
                phgCache.put(pg.getPhoneNumber(), pg);
            }
            results.close();
        }

         /* Обрабатываем очищенные данные - сохраняем сообщения в БД */
        MessageDAO msgDAO = new MessageDAO(session);
        List<Message> dbMessages = new ArrayList<>();
        String bulkDescription = data.getDescription();

        //Long messageId = null;

        try {
            session.getTransaction().begin();

            String msgChannels;
            long msgScenarioId;

            for (Iterator<MessageCreationRequestObject.Message> iterMessage = data.getMessages().listIterator(); iterMessage.hasNext(); ) {
                MessageCreationRequestObject.Message message = iterMessage.next();

                /* Если есть персоналные каналы для телефона, то используем их,
                 * но с учетом настроек типа сообщения */
                msgChannels = (message.getChannels()==null? baseChannels: channelsAnd(message.getChannels(), msgType));

                /* Обрабатываем гранты для телефонов */
                if (phgCache.containsKey(message.getPhoneNumber())){
                    PhoneGrant pg = phgCache.get(message.getPhoneNumber());

                    /* проверка включенных каналов на телефоне */
                    msgChannels = channelsAnd(msgChannels, pg);

                }

                /* Если каналов нет, то ошибка */
                if (msgChannels.length()==0){
                    iterMessage.remove();
                    failedMessages.add(
                            new MessageCreationResponseObject.FailedMessage(
                                    message.getMessageId(),
                                    "Требуемые каналы недоступны"
                            )
                    );
                    iterMessage.remove();
                    continue;
                }

                /* Если сценарии совпадают с базовым, то базовый идентификатор сценария,
                *  если не совпадает - ищем в кэше или запрашиваем из БД */

                if (msgChannels.equalsIgnoreCase(baseChannels)){
                    msgScenarioId = baseScenarioId;
                } else {
                    if (scCache.containsKey(msgChannels)){
                        msgScenarioId = scCache.get(msgChannels);
                    } else {

                        Scenario sc = scenarioDAO.getByChannels(msgChannels, infobipLogin);
                        if ( sc == null ) {
                            iterMessage.remove();
                            failedMessages.add(
                                    new MessageCreationResponseObject.FailedMessage(
                                            message.getMessageId(),
                                            "Подходящий сценарий на каналы " + msgChannels + " не найден"
                                    )
                            );
                            iterMessage.remove();
                            continue;
                        } else {
                            msgScenarioId = sc.getId();
                            scCache.put(msgChannels, msgScenarioId);
                        }
                    }
                }

                Message msg = new Message();
                msg.setScenarioId(msgScenarioId);
                msg.setTypeId(messageType);
                msg.setUserId(userId);

                msg.setPhoneNumber(message.getPhoneNumber());

                if (msgChannels.contains("S")) msg.setSmsText(message.getSmsText());
                if (msgChannels.contains("V")) msg.setViberText(message.getViberText());
                if (msgChannels.contains("O")) msg.setVoiceText(message.getVoiceText());
                if (msgChannels.contains("P")) msg.setParsecoText(message.getParsecoText());
                if (msgChannels.contains("F")) msg.setFacebookText(message.getFacebookText());

                msgDAO.add(msg);

                msg.setExternalId(msgPrefix+msg.getId());
                dbMessages.add(msg);

                successMessages.add(
                        new MessageCreationResponseObject.SuccessMessage(
                                message.getMessageId(),
                                msgPrefix+msg.getId()
                        )
                );

            }


            for (Message msg : dbMessages){
                msgDAO.save(msg);
            }

            Report report;
            ReportDAO reportDAO = new ReportDAO(session);
            for (Message msg : dbMessages){
                //Создаём отчёт
                report = new Report(
                        msg.getId(),
                        "ADD_TO_QUEUE",
                        "ADD_TO_QUEUE",
                        "Сообщение добавлено в очередь"
                );
                reportDAO.add(report);
            }

            QueuedMessageDAO queuedMessageDAO = new QueuedMessageDAO(session);
            BulkDAO bulkDAO = new BulkDAO(session);
            BulkMessageDAO bulkMessageDAO = new BulkMessageDAO(session);

            // если не задан идентификатор рассылки и задано описание рассылки, то генерируем идентификатор
            if (bulkId==null&&(bulkDescription!=null&&bulkDescription.length()>0)) {
                Bulk bulk = new Bulk(bulkDescription);
                bulkDAO.add(bulk);
                bulkId = bulk.getId();

            }

            if (bulkId!=null) {

                BulkMessage bulkMessage = new BulkMessage();
                for (Message msg : dbMessages){
                    bulkMessage.setBulkId(bulkId);
                    bulkMessage.setMessageId(msg.getId());
                    bulkMessageDAO.add(bulkMessage);
                }

            }

            QueuedMessage queuedMessage = new QueuedMessage();
            for (Message msg : dbMessages){
                    queuedMessage.setMessageId(msg.getId());
                    queuedMessageDAO.add(queuedMessage);
            }

            session.getTransaction().commit();

        } catch (JDBCException e) {
            session.getTransaction().rollback();
            String error = e.getSQLException().getMessage();
            for (MessageCreationRequestObject.Message message : data.getMessages()) {
                failedMessages.add(
                        new MessageCreationResponseObject.FailedMessage(
                                message.getMessageId(),
                                error
                        )
                );
                successMessages.clear();
            }
        }
        return new MessageCreationResponseObject(successMessages, failedMessages, bulkId, bulkDescription);
    }

    private static String channelsAnd(String channels, AcceptChannels acceptChannels){
        if (!acceptChannels.isAcceptSms()) channels = channels.replace("S","");
        if (!acceptChannels.isAcceptViber()) channels = channels.replace("V","");
        if (!acceptChannels.isAcceptVoice()) channels = channels.replace("O","");
        if (!acceptChannels.isAcceptParseco()) channels = channels.replace("P","");
        if (!acceptChannels.isAcceptFacebook()) channels = channels.replace("F","");
        return channels;
    }
}
