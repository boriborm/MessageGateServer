package com.bankir.mgs;

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

    static final int MAX_MESSAGES=1000;

    private static final int SCENARIO_OK = 1;
    private static final int SCENARIO_INVALID = 2;

    private static final int MESSAGE_TYPE_OK = 1;
    private static final int MESSAGE_TYPE_INVALID = 2;

    private static final Logger logger = LoggerFactory.getLogger(MessageGenerator.class);

    static class VolumeException extends Exception
    {
        //Constructor that accepts a message
        VolumeException(String message)
        {
            super(message);
        }
    }

    public static MessageCreationResponseObject Generate(StatelessSession session, MessageCreationRequestObject data, User user, Long bulkId) throws VolumeException {

        Long userId = user.getId();
        String msgPrefix = Config.getSettings().getMessageIdPrefix();
        if (msgPrefix!=null&&msgPrefix.length()>0) msgPrefix = msgPrefix+"-";
        else msgPrefix = "";

        /* Проверим количество сообщений */
        if (data.getMessages().size()>MAX_MESSAGES) throw new VolumeException("Количество сообщений не должно превышать "+MAX_MESSAGES);

        List<MessageCreationResponseObject.SuccessMessage> successMessages = new ArrayList<>();
        List<MessageCreationResponseObject.FailedMessage> failedMessages = new ArrayList<>();

        //StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        //Проверка правильности ключа сценария
        ScenarioDAO scenarioDAO = new ScenarioDAO(session);
        String scenarioKey = data.getScenarioKey();
        if (scenarioKey==null) scenarioKey = Config.getSettings().getDefaultScenarioKey();
        String infobipLogin = Config.getSettings().getInfobip().getLogin();
        Scenario scenario = scenarioDAO.getByKey(scenarioKey, infobipLogin);
        if ( scenario == null ){
            return new MessageCreationResponseObject("Сценарий не найден");
        }
        scenario.parseFlow();

        //Проверка правильности типа сообщения
        MessageTypeDAO msgTypeDAO = new MessageTypeDAO(session);
        String messageType = data.getMessageType();
        if (messageType==null) messageType = Config.DEFAULT_MESSAGE_TYPE;

        MessageType msgType = msgTypeDAO.getById(messageType);

        if (msgType == null) {
            return new MessageCreationResponseObject("Тип сообщения "+messageType+" не найден");
        }

        if (!msgType.isActive()) {
            return new MessageCreationResponseObject("Тип сообщения "+messageType+" не активен");
        }

        UserMessageTypeDAO userMessageTypeDAO = new UserMessageTypeDAO(session);
        UserMessageType umt = userMessageTypeDAO.get(messageType, user.getId());
        if (umt == null){
            return new MessageCreationResponseObject("У пользователя "+user.getLogin()+" нет доступа к типу сообщения " + messageType);
        }


        boolean toSms = data.isToSms() && scenario.isSms() && msgType.isAcceptSms();
        boolean toViber = data.isToViber() && scenario.isViber() && msgType.isAcceptViber();
        boolean toVoice = data.isToVoice() && scenario.isVoice() && msgType.isAcceptVoice();
        boolean toParseco = data.isToParseco() && scenario.isParseco() && msgType.isAcceptParseco();

        if (!(toSms || toViber || toVoice || toParseco)) {
            return new MessageCreationResponseObject("Отсутствуют доступные каналы для рассылки");
        }

        HashMap<String, PhoneGrant> phgCache = new HashMap<>();

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


            boolean msgSms;
            boolean msgViber;
            boolean msgVoice;
            boolean msgParseco;

            for (Iterator<MessageCreationRequestObject.Message> iterMessage = data.getMessages().listIterator(); iterMessage.hasNext(); ) {
                MessageCreationRequestObject.Message message = iterMessage.next();

                if (phgCache.containsKey(message.getPhoneNumber())){
                    PhoneGrant pg = phgCache.get(message.getPhoneNumber());

                    /* проверка включенных каналов на телефоне */
                    msgSms = pg.isAcceptSms() && toSms;
                    msgViber = pg.isAcceptViber() && toViber;
                    msgVoice = pg.isAcceptVoice() && toVoice;
                    msgParseco = pg.isAcceptParseco() && toParseco;
                    if (!(msgSms||msgViber||msgVoice||msgParseco)){
                        iterMessage.remove();
                        failedMessages.add(
                                new MessageCreationResponseObject.FailedMessage(
                                        message.getMessageId(),
                                        "У телефона требуемые каналы заблокированы"
                                )
                        );
                        iterMessage.remove();
                        continue;
                    }
                } else {
                    msgSms = toSms;
                    msgViber = toViber;
                    msgVoice = toVoice;
                    msgParseco = toParseco;
                }

                Message msg = new Message();
                msg.setScenarioId(scenario.getId());
                msg.setTypeId(messageType);
                msg.setUserId(userId);

                msg.setPhoneNumber(message.getPhoneNumber());

                if (msgSms) msg.setSmsText(message.getSmsText());
                if (msgViber) msg.setViberText(message.getViberText());
                if (msgVoice) msg.setVoiceText(message.getVoiceText());
                if (msgParseco) msg.setParsecoText(message.getParsecoText());

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
}
