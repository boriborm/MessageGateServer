package com.bankir.mgs;

import com.bankir.mgs.hibernate.dao.*;
import com.bankir.mgs.hibernate.model.*;
import com.bankir.mgs.jersey.model.CreateMessageRequestObject;
import com.bankir.mgs.jersey.model.CreateMessageResponseObject;
import com.bankir.mgs.jersey.model.CreateMessagesRequestObject;
import com.bankir.mgs.jersey.model.CreateMessagesResponseObject;
import org.hibernate.JDBCException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.*;

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

    public static CreateMessagesResponseObject generate(CreateMessagesRequestObject data, User user) throws VolumeException {

        Long userId = user.getId();
        String msgPrefix = Config.getSettings().getMessageIdPrefix();
        if (msgPrefix!=null&&msgPrefix.length()>0) msgPrefix = msgPrefix+"-";
        else msgPrefix = "";

        /* Проверим количество сообщений */
        if (data.getMessages().size()>MAX_MESSAGES) throw new VolumeException("Количество сообщений не должно превышать "+MAX_MESSAGES);

        List<CreateMessageResponseObject> responseMessageDataList = new ArrayList<>();

        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

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

//        System.out.println(new Date().toLocaleString() + " start");

        /* Проверки */

        //Список телефонов для выборки из PhoneGrant
        List<String> listPhones = new ArrayList<>();

        for (Iterator<CreateMessageRequestObject> iterMessage = data.getMessages().listIterator(); iterMessage.hasNext(); ) {

            CreateMessageRequestObject message = iterMessage.next();

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
                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                false,
                                "Сценарий не найден",
                                null
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
                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                false,
                                "В сценарии требуемые каналы отсутствуют",
                                null
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
                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                false,
                                "Тип сообщения " + messageType + " не допустим",
                                null
                        )
                );
                iterMessage.remove();
                continue;
            }

            if (!msgType.isActive()) {
                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                false,
                                "Тип сообщения " + messageType + " не активен",
                                null
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
                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                false,
                                "В типе сообщения " + messageType + " требуемые каналы заблокированы",
                                null
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
                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                false,
                                "Некорректный номер телефона",
                                null
                        )
                );
                iterMessage.remove();
                continue;
            }

            /* Добавляем в список телефонов для отправки */
            listPhones.add(phone);
        }
        System.out.println(new Date().toLocaleString() + " verified. Start phonegrant caching");

        if (data.getMessages().size()==0) {
            session.close();
            return new CreateMessagesResponseObject(responseMessageDataList, null, null);
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

        System.out.println(new Date().toLocaleString() + " begin save messages");
        /* Бежим по списку сообщений, проверяем на гранты телефона и формируем список сообщений для БД,
         * сохраняя основное сообщение */
        for (Iterator<CreateMessageRequestObject> iterMessage = data.getMessages().listIterator(); iterMessage.hasNext(); ) {
            CreateMessageRequestObject message = iterMessage.next();
            if (phgCache.containsKey(message.getPhoneNumber())){
                PhoneGrant pg = phgCache.get(message.getPhoneNumber());

                /* проверка включенных каналов на телефоне */
                toSms = pg.isAcceptSms() && message.toSms();
                toViber = pg.isAcceptViber() && message.toViber();
                toVoice = pg.isAcceptVoice() && message.toVoice();
                toParseco = pg.isAcceptParseco() && message.toParseco();
                if (!(toSms || toViber || toVoice || toParseco)) {
                    iterMessage.remove();
                    responseMessageDataList.add(
                            new CreateMessageResponseObject(
                                    message.getMessageId(),
                                    false,
                                    "У телефона требуемые каналы заблокированы",
                                    null
                            )
                    );
                }
            }
        }

        /* Обрабатываем очищенные данные - сохраняем сообщения в БД */
        MessageDAO msgDAO = new MessageDAO(session);
        List<Message> dbMessages = new ArrayList<>();
        Long bulkId = null;
        String bulkDescription = data.getDescription();

        try {
            session.getTransaction().begin();

            for (Message msg : dbMessages){
                msgDAO.save(msg);
            }

            for (CreateMessageRequestObject message:data.getMessages() ) {

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

                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                true,
                                null,
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
            System.out.println(new Date().toLocaleString() + " reports saved. Start queuing");


            QueuedMessageDAO queuedMessageDAO = new QueuedMessageDAO(session);
            BulkDAO bulkDAO = new BulkDAO(session);
            BulkMessageDAO bulkMessageDAO = new BulkMessageDAO(session);
            QueuedBulkDAO queuedBulkDAO = new QueuedBulkDAO(session);
            QueuedBulkMessageDAO queuedBulkMessageDAO = new QueuedBulkMessageDAO(session);



            boolean signal = (dbMessages.size()>0);
            boolean isBulk = false;


            if (bulkDescription!=null&&bulkDescription.length()>0) {
                isBulk=true;
            }



            if (isBulk)
            {
                BulkMessage bulkMessage = new BulkMessage();
                Bulk bulk = new Bulk(bulkDescription);
                bulkDAO.add(bulk);
                bulkId = bulk.getId();

                for (Message msg : dbMessages){
                    bulkMessage.setBulkId(bulkId);
                    bulkMessage.setMessageId(msg.getId());
                    bulkMessageDAO.add(bulkMessage);
                }

                QueuedBulkMessage queuedBulkMessage = new QueuedBulkMessage();
                for (Message msg : dbMessages){
                    queuedBulkMessage.setBulkId(bulkId);
                    queuedBulkMessage.setMessageId(msg.getId());
                    queuedBulkMessageDAO.add(queuedBulkMessage);
                }
                queuedBulkDAO.add(new QueuedBulk(bulkId));
            } else {
                QueuedMessage queuedMessage = new QueuedMessage();
                for (Message msg : dbMessages){
                    queuedMessage.setMessageId(msg.getId());
                    queuedMessageDAO.add(queuedMessage);
                }
            }
//            System.out.println(new Date().toLocaleString() + " queued");

            session.getTransaction().commit();

            //Сигнализируем процессу обработки очереди о необходимости начать обрабтку
            if (signal) QueueProcessor.getInstance().signal();

        } catch (JDBCException e) {
            session.getTransaction().rollback();
            String error = e.getSQLException().getMessage();
            for (CreateMessageRequestObject message : data.getMessages()) {
                responseMessageDataList.add(
                        new CreateMessageResponseObject(
                                message.getMessageId(),
                                false,
                                error,
                                null
                        )
                );
            }
        }

        session.close();
        return new CreateMessagesResponseObject(responseMessageDataList, bulkId, bulkDescription);
    }
}
