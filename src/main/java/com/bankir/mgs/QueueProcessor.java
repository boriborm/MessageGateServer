package com.bankir.mgs;


import com.bankir.mgs.hibernate.dao.*;
import com.bankir.mgs.hibernate.model.*;
import com.bankir.mgs.hibernate.model.Message;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.*;
import org.hibernate.JDBCException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class QueueProcessor  extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageGenerator.class);
    private static QueueProcessor qp;
    private static MessageType defaultMessageType = new MessageType();

    public static synchronized QueueProcessor getInstance(){
        if (qp ==null) qp = new QueueProcessor();
        return qp;
    }

    /* Обработка очереди сообщений */
    protected void process() throws Exception {

        boolean signalDeliveryProcessor = false;

        InfobipMessageGateway ims = null;

        StatelessSession sessionForQueries = Config.getHibernateSessionFactory().openStatelessSession();
        StatelessSession sessionForTransactions = Config.getHibernateSessionFactory().openStatelessSession();

        MessageDAO msgDAO = new MessageDAO(sessionForQueries);
        QueuedMessageDAO qmDAO = new QueuedMessageDAO(sessionForTransactions);
        ReportDAO reportDAO = new ReportDAO(sessionForTransactions);
        /* Обрабатываем одиночные сообщения */
        Query query = sessionForQueries.createQuery(
                "FROM Message m " +
                "JOIN MessageType mt ON m.typeId=mt.typeId " +
                "JOIN QueuedMessage qm ON m.id=qm.messageId " +
                "JOIN Scenario s ON m.scenarioId=s.id "
        );
        query.setFetchSize(1000);
        query.setReadOnly(true);
        ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
        try{
            while (results.next()) {

                //System.out.println("process message");
                if (ims == null) ims = new InfobipMessageGateway();

                // Данные сообщения
                Message msg = (Message) results.get(0);
                QueuedMessage qmsg = (QueuedMessage) results.get(2);

                // Данные типа сообщения
                MessageType msgType = (MessageType) results.get(1);
                if (msgType == null) msgType = defaultMessageType;

                // Данные сценария
                Scenario scenario = (Scenario) results.get(3);

                String smsText = msg.getSmsText();
                String viberText = msg.getViberText();
                String parsecoText = msg.getParsecoText();
                String voiceText = msg.getVoiceText();

                if (msgType.isVerifyImsi()){
                    if (imsiChanged(sessionForTransactions, ims, msg.getPhoneNumber())){
                        smsText = viberText = parsecoText = voiceText = Config.getSettings().getImsiChangedMessage();
                    }
                }

                OmniAdvancedMessage advMsg = new OmniAdvancedMessage(scenario.getScenarioKey(), null, msg.getSendAt());

                /* Добавляем отправку по СМС */
                advMsg.addSms(smsText, msgType.getSmsValidityPeriod());
                /* Добавляем отправку по Viber */
                advMsg.addViber(viberText, msgType.getViberValidityPeriod());
                /* Добавляем отправку по Parseco */
                advMsg.addParseo(parsecoText, msgType.getParsecoValidityPeriod());
                /* Добавляем отправку по Voice */
                advMsg.addVoice(voiceText, msgType.getVoiceValidityPeriod());

                Destination destination = new Destination(msg.getExternalId(), msg.getPhoneNumber(), /*(sendToEmail ? msg.getEmailAddress() : null)*/ null);
                advMsg.addDestination(destination);

                MessagesResponse messagesResponse = ims.sendAdvancedMessage(advMsg);

                if (messagesResponse.getMessages().size() > 0) {

                    sessionForTransactions.getTransaction().begin();

                    Status status;
                    try {

                        // Удаляем сообщение из очереди рассылки
                        qmDAO.delete(qmsg);

                    } catch (JDBCException e) {
                        sessionForTransactions.getTransaction().rollback();
                    }


                    try {
                        for (com.bankir.mgs.infobip.model.Message infobipMessage:messagesResponse.getMessages()) {
                            status = infobipMessage.getStatus();
                            Long msgId = null;
                            if (msg.getExternalId().equals(infobipMessage.getMessageId())) {
                                msgId = msg.getId();
                            }else{

                                Message msg2 = msgDAO.getByExternalId(infobipMessage.getMessageId());
                                if (msg2!=null){
                                    msgId = msg2.getId();
                                }
                            }

                            if (msgId!=null) {
                                Report report = new Report(
                                        msgId,
                                        status.getName(),
                                        status.getGroupName(),
                                        status.getDescription()
                                );
                                //Добавляем отчёт об отправке
                                reportDAO.add(report);
                            }
                        }


                        sessionForTransactions.getTransaction().commit();
                    } catch (JDBCException e) {
                        sessionForTransactions.getTransaction().rollback();
                    }

                    signalDeliveryProcessor = true;
                }
            }
        } catch (InfobipMessageGateway.RequestErrorException requestErrorException){

            logger.error("Send Message Error: "+ requestErrorException.getMessage(), requestErrorException);
            // В случае ошибок URL_ERROR, PROXY_ERROR, AUTH_ERROR - неверные настройки сервера. Процесс рассылки останавливается
            // до исправления

            if (   requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.URL_ERROR)
                 ||requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.PROXY_ERROR)
                 ||requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.AUTH_ERROR)
               ){

                results.close();
                sessionForQueries.close();
                sessionForTransactions.close();
                if (ims!=null) ims.stop();
                throw requestErrorException;
            }
        }
        results.close();
        sessionForQueries.close();
        sessionForTransactions.close();
        if (ims!=null) ims.stop();

        // Уменьшим период запроса отчета до минимума
        // Сигналить нет смысла, т.к. сообщения ещё могут быть не отправлены, нужно некотрое время
        if (signalDeliveryProcessor){
            DeliveryReportProcessor drp = DeliveryReportProcessor.getInstance();

            drp.setSleepTime(DeliveryReportProcessor.MIN_SLEEP_TIME);
//            drp.signal();
        }

    }

    private boolean imsiChanged(StatelessSession session, InfobipMessageGateway img, String phoneNumber) throws InfobipMessageGateway.RequestErrorException {

        //Получаем текущий имси
        //Смотрим в БД
        //Если не найден, то добавляем и возвращаем false
        //Если найден, сравниваем с текущим и если совпадает, то возвращаем false
        //Если найден и не совпадает, то сохраняем новое значение и возвращаем true
        ImsiRequest imsiRequest = new ImsiRequest(phoneNumber);
        ImsiResponse imsiResponse = img.getImsi(imsiRequest);
        String currentImsi = imsiResponse.getImsi();

        boolean changed = false;
        if (currentImsi!=null){

            Query query = session.createQuery(
                        "FROM Imsi i" +
                        "LEFT JOIN NewImsi ni ON i.phoneNumber=ni.phoneNumber " +
                        "WHERE i.phoneNumber = :phoneNumber"
                    )
                    .setParameter("phoneNumber", phoneNumber)
                    .setReadOnly(true);

            ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);

            if (results.next()) {

                Imsi imsi = (Imsi) results.get(0);
                NewImsi newimsi = (NewImsi) results.get(1);

                // Если фиксированный имси отличется от полученного, значит была замена
                if (!imsi.getImsi().equals(currentImsi)){
                    changed = true;

                    //обновляем или добавляем в список новых имси
                    if (newimsi==null||( !newimsi.getImsi().equals(currentImsi) ) ){
                        try {
                            NewImsiDAO dao = new NewImsiDAO(session);
                            NewImsi saveImsi = new NewImsi(phoneNumber, currentImsi);
                            session.getTransaction().begin();
                            // Если замена еще не зафиксирована, то фиксируем в новых имси
                            if (newimsi==null) {
                                dao.add(saveImsi);
                            } else {
                                dao.save(saveImsi);
                            }
                            session.getTransaction().commit();
                        }catch (JDBCException e){
                            session.getTransaction().rollback();
                            logger.error("Error: "+e.getSQLException().getMessage(), e);
                        }
                    }

                }
            } else {
                // Фиксируем Imsi в БД
                try {
                    ImsiDAO dao = new ImsiDAO(session);
                    Imsi imsi = new Imsi(phoneNumber, currentImsi);
                    session.getTransaction().begin();
                    dao.add(imsi);
                    session.getTransaction().commit();
                }catch (JDBCException e){
                    session.getTransaction().rollback();
                    logger.error("Error: " + e.getSQLException().getMessage(), e);
                }

            }
            results.close();
        }
        return changed;
    }
}
