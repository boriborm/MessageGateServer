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

        ScenarioDAO scenarioDAO = new ScenarioDAO(session);
        MessageTypeDAO msgTypeDAO = new MessageTypeDAO(session);

        Scenario scenario = null;
        MessageType msgType = null;

        String scenarioKey;
        String infobipLogin = Config.getSettings().getInfobip().getLogin();
        String messageType;

        boolean toSms;
        boolean toViber;
        boolean toVoice;
        boolean toParseco;

        HashMap<String, MessageType> mtCache = new HashMap<>();
        HashMap<String, Scenario> scCache = new HashMap<>();
        HashMap<String, PhoneGrant> phgCache = new HashMap<>();


        List<String> wrongScenarios = new ArrayList<>();
        List<String> wrongMessageTypes = new ArrayList<>();

        /* Бежим по списку сообщений и проверяем правильность оформления:
        * 1. сценария
        * 2. типа сообщения
        * 3. номера телефона
        * 4. каналы для отправки сценарий, тип, гранты на телефон
        * */

        int scenarioStatus;
        int messageTypeStatus;

        int countIncomeMessages = data.getMessages().size();

//        System.out.println(new Date().toLocaleString() + " start");

        /* Проверки */

        //Список телефонов для выборки из PhoneGrant
        List<String> listPhones = new ArrayList<>();

        for (Iterator<MessageCreationRequestObject.Message> iterMessage = data.getMessages().listIterator(); iterMessage.hasNext(); ) {

            MessageCreationRequestObject.Message message = iterMessage.next();

            /* проверка включенных каналов на сообщении, если нет, то ставим по умолчанию SMS */
            toSms = message.toSms();
            toViber = message.toViber();
            toVoice = message.toVoice();
            toParseco = message.toParseco();
            if (!(toSms || toViber || toVoice || toParseco)) toSms = true;


            /* 1. Проверка сценария */
            scenarioKey = message.getScenarioKey();
            scenarioStatus = SCENARIO_OK;
            /* Проверяем нет ли сценария в неправильных */
            if (wrongScenarios.contains(scenarioKey)) {
                scenarioStatus = SCENARIO_INVALID;
            } else {

                /* Если у последний обработанный сценарий, сохраненный в scenario тот же самый, что и обрабатываемый, то
                * используем scenario, иначем считываем из кэша или из БД в scenario */
                if (scenario == null || !(scenario.getScenarioKey().equals(scenarioKey))) {
                    if (scCache.containsKey(scenarioKey)) {
                        scenario = scCache.get(scenarioKey);
                    } else {
                        scenario = scenarioDAO.getByKey(scenarioKey, infobipLogin);
                        if (scenario == null) {
                            wrongScenarios.add(scenarioKey);
                            scenarioStatus = SCENARIO_INVALID;
                        } else {
                            scCache.put(scenarioKey, scenario);
                        }
                    }
                }
            }

            /* если сценарий не удалось определить, значит он не валидный */
            if (scenarioStatus == SCENARIO_INVALID) {
                failedMessages.add(
                        new MessageCreationResponseObject.FailedMessage(
                            message.getMessageId(),
                            "Сценарий не найден"
                        )
                );
                iterMessage.remove();
                continue;
            }


            /* Проверка включенных каналов на сценарии */
            //toSms = scenario.isAcceptSms() && toSms;
            //toViber = scenario.isAcceptViber() && toViber;
            //toVoice = scenario.isAcceptVoice() && toVoice;
            //toParseco = scenario.isAcceptParseco() && toParseco;

            //если ни один канал не активировался, тогда ошибка
            if (!(toSms || toViber || toVoice || toParseco)) {
                failedMessages.add(
                        new MessageCreationResponseObject.FailedMessage(
                                message.getMessageId(),
                                "В сценарии требуемые каналы отсутствуют"
                        )
                );
                iterMessage.remove();
                continue;
            }

            message.setScenarioId(scenario.getId());

            /* 2. Проверка MessageType */
            messageType = message.getMessageType();
            messageTypeStatus = MESSAGE_TYPE_OK;

            /* Проверяем нет ли сценария в неправильных */
            if (wrongMessageTypes.contains(messageType)) {
                messageTypeStatus = MESSAGE_TYPE_INVALID;
            }


            if (messageTypeStatus == MESSAGE_TYPE_OK) {

                if (msgType == null || !msgType.getTypeId().equals(messageType)) {
                    if (mtCache.containsKey(messageType)) {
                        msgType = mtCache.get(messageType);
                    } else {
                        msgType = msgTypeDAO.getById(messageType);

                        if (msgType == null) {
                            wrongMessageTypes.add(messageType);
                            messageTypeStatus = SCENARIO_INVALID;
                        } else {
                            mtCache.put(messageType, msgType);
                        }
                    }
                }

            }

            if (messageTypeStatus == SCENARIO_INVALID) {
                failedMessages.add(
                        new MessageCreationResponseObject.FailedMessage(
                                message.getMessageId(),
                                "Тип сообщения " + messageType + " не допустим"
                        )
                );
                iterMessage.remove();
                continue;
            }

            if (!msgType.isActive()) {
                failedMessages.add(
                        new MessageCreationResponseObject.FailedMessage(
                                message.getMessageId(),
                                "Тип сообщения " + messageType + " не активен"
                        )
                );
                iterMessage.remove();
                continue;
            }



            /* проверка включенных каналов на типе сообщения */
            toSms = msgType.isAcceptSms() && toSms;
            toViber = msgType.isAcceptViber() && toViber;
            toVoice = msgType.isAcceptVoice() && toVoice;
            toParseco = msgType.isAcceptParseco() && toParseco;

            //если ни один канал не активировался, тогда ошибка
            if (!(toSms || toViber || toVoice || toParseco)) {
                failedMessages.add(
                        new MessageCreationResponseObject.FailedMessage(
                                message.getMessageId(),
                                "В типе сообщения " + messageType + " требуемые каналы заблокированы"
                        )
                );
                iterMessage.remove();
                continue;
            }



            /* Оставляем в данных сообщения только активировавшиеся каналы */
            message.setToSms(toSms);
            message.setToParseco(toParseco);
            message.setToVoice(toVoice);
            message.setToViber(toViber);


            /*3. Проверка правильности номеров телефонов (длина 11 символов) */

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

        /* Если все сообщения отбраковались, то прерываем дальнейшую обработку */
        if (countIncomeMessages == failedMessages.size()) {
            //session.close();
            return new MessageCreationResponseObject(null, failedMessages);
        }

        /* Кэшируем гранты на телефоны */
        Query phoneGrantsQuery = session.createQuery("From PhoneGrant pg where pg.phoneNumber in (:listPhones)")
                .setParameter("listPhones", listPhones);
        ScrollableResults results = phoneGrantsQuery.scroll(ScrollMode.FORWARD_ONLY);
        while (results.next()) {
            PhoneGrant pg = (PhoneGrant) results.get(0);
            //Добавляем в кэш
            phgCache.put(pg.getPhoneNumber(), pg);
        }
        results.close();

        /* Бежим по списку сообщений, проверяем на гранты телефона и формируем список сообщений для БД,
         * сохраняя основное сообщение */
        for (Iterator<MessageCreationRequestObject.Message> iterMessage = data.getMessages().listIterator(); iterMessage.hasNext(); ) {
            MessageCreationRequestObject.Message message = iterMessage.next();
            if (phgCache.containsKey(message.getPhoneNumber())){
                PhoneGrant pg = phgCache.get(message.getPhoneNumber());

                /* проверка включенных каналов на телефоне */
                toSms = pg.isAcceptSms() && message.toSms();
                toViber = pg.isAcceptViber() && message.toViber();
                toVoice = pg.isAcceptVoice() && message.toVoice();
                toParseco = pg.isAcceptParseco() && message.toParseco();
                if (!(toSms || toViber || toVoice || toParseco)) {
                    iterMessage.remove();
                    failedMessages.add(
                            new MessageCreationResponseObject.FailedMessage(
                                    message.getMessageId(),
                                    "У телефона требуемые каналы заблокированы"
                            )
                    );
                }
            }
        }

        /* Обрабатываем очищенные данные - сохраняем сообщения в БД */
        MessageDAO msgDAO = new MessageDAO(session);
        List<Message> dbMessages = new ArrayList<>();
        String bulkDescription = data.getDescription();

        //Long messageId = null;

        try {
            session.getTransaction().begin();

            /*
            if (data.getMessages().size()<10){
                Query query = session.createQuery("select max(m1.id) from Message m1 where not exists(select m2.id from Message m2 where m2.id between m1.id+1 and m1.id+:sz) and exists(select m3.id from Message m3 where m3.id>m1.id)")
                        .setParameter("sz", ((Integer) data.getMessages().size()).longValue());

                Object result = query.uniqueResult();
                if (result!=null){
                    messageId = (Long) result + 1;
                }
            }
            */

            for (MessageCreationRequestObject.Message message:data.getMessages() ) {

                Message msg = new Message();
                msg.setScenarioId(message.getScenarioId());
                msg.setTypeId(message.getMessageType());
                msg.setUserId(userId);

                msg.setPhoneNumber(message.getPhoneNumber());

                if (message.toSms()) msg.setSmsText(message.getSmsText());
                if (message.toViber()) msg.setViberText(message.getViberText());
                if (message.toVoice()) msg.setVoiceText(message.getVoiceText());
                if (message.toParseco()) msg.setParsecoText(message.getParsecoText());

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


//            System.out.println(new Date().toLocaleString() + "messages saved. Update messages extarnalIds");
            for (Message msg : dbMessages){
                msgDAO.save(msg);
            }
//            System.out.println(new Date().toLocaleString() + "externalIds updates. Start save reports");

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

                /*
                QueuedBulkMessage queuedBulkMessage = new QueuedBulkMessage();
                for (Message msg : dbMessages){
                    queuedBulkMessage.setBulkId(bulkId);
                    queuedBulkMessage.setMessageId(msg.getId());
                    queuedBulkMessageDAO.add(queuedBulkMessage);
                }
                */


            } else {
                QueuedMessage queuedMessage = new QueuedMessage();
                for (Message msg : dbMessages){
                    queuedMessage.setMessageId(msg.getId());
                    queuedMessageDAO.add(queuedMessage);
                }
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

    public static void CreateBulkQueue(StatelessSession session, Long bulkId){
        session.getTransaction().begin();

        QueuedBulkDAO queuedBulkDAO = new QueuedBulkDAO(session);
        queuedBulkDAO.add(new QueuedBulk(bulkId));
        Query query = session.createQuery("insert into QueuedBulkMessage(bulkId, messageId) select bm.bulkId, bm.messageId from BulkMessage bm where bm.bulkId = :bulkId");
        query.setParameter("bulkId", bulkId);
        query.executeUpdate();
        session.getTransaction().commit();
    }
}
