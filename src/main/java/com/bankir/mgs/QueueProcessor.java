package com.bankir.mgs;


import com.bankir.mgs.hibernate.dao.QueuedMessageDAO;
import com.bankir.mgs.hibernate.dao.ReportDAO;
import com.bankir.mgs.hibernate.model.*;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.Destination;
import com.bankir.mgs.infobip.model.MessagesResponse;
import com.bankir.mgs.infobip.model.OmniAdvancedMessage;
import com.bankir.mgs.infobip.model.Status;
import org.hibernate.JDBCException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.Date;

public class QueueProcessor  extends AbstractProcessor {

    private static QueueProcessor qp;
    private static PhoneGrant defaultPhoneGrant = new PhoneGrant();
    private static MessageType defaultMessageType = new MessageType();

    public static synchronized QueueProcessor getInstance(){
        if (qp ==null) qp = new QueueProcessor();
        return qp;
    }

    /* Обработка очереди сообщений */
    protected void process() throws Exception {

        boolean signalDeliveryProcessor = false;

        System.out.println("process queue " + (new Date()).toString());
        InfobipMessageGateway ims = null;

        StatelessSession sessionForQueries = Config.getHibernateSessionFactory().openStatelessSession();
        StatelessSession sessionForTransactions = Config.getHibernateSessionFactory().openStatelessSession();

        QueuedMessageDAO qmDAO = new QueuedMessageDAO(sessionForTransactions);
        ReportDAO reportDAO = new ReportDAO(sessionForTransactions);
        /* Обрабатываем одиночные сообщения */
        Query query = sessionForQueries.createQuery(
                "FROM Message m " +
                "JOIN MessageType mt ON m.typeId=mt.typeId " +
                "JOIN QueuedMessage qm ON m.id=qm.messageId " +
                "JOIN Scenario s ON m.scenarioId=s.id " +
                "LEFT JOIN PhoneGrant pg ON pg.phoneNumber = m.phoneNumber "
        );
        query.setFetchSize(1000);
        query.setReadOnly(true);
        ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);

        while (results.next()) {

            //System.out.println("process message");

            // Данные сообщения
            Message msg = (Message) results.get(0);
            QueuedMessage qmsg = (QueuedMessage) results.get(2);

            // Данные типа сообщения
            MessageType msgType = (MessageType) results.get(1);
            if (msgType == null) msgType = defaultMessageType;

            // Данные сценария
            Scenario scenario = (Scenario) results.get(3);

            //Грант на телефон
            PhoneGrant phGrant = (PhoneGrant) results.get(4);
            if (phGrant == null) phGrant = defaultPhoneGrant;

            OmniAdvancedMessage advMsg = new OmniAdvancedMessage(scenario.getScenarioKey(), null, msg.getSendAt());

            // Отправка только в случае, если номер телефона задан
            boolean sendToPhone = false;

            if (msg.getPhoneNumber() != null) {


                /* Добавляем отправку по СМС */
                if (phGrant.isAcceptSms() && msgType.isAcceptSms()) {
                        advMsg.addSms(msg.getSmsText(), msgType.getSmsValidityPeriod());
                        sendToPhone = true;
                }

                /* Добавляем отправку по Viber */
                if (phGrant.isAcceptViber() && msgType.isAcceptViber()) {
                        advMsg.addViber(msg.getViberText(), msgType.getViberValidityPeriod());
                        sendToPhone = true;
                }

                /* Добавляем отправку по Parseco */
                if (phGrant.isAcceptParseco() && msgType.isAcceptParseco()) {
                        advMsg.addViber(msg.getParsecoText(), msgType.getParsecoValidityPeriod());
                        sendToPhone = true;
                }

                /* Добавляем отправку по Voice */
                if (phGrant.isAcceptVoice() && msgType.isAcceptVoice()) {
                        advMsg.addViber(msg.getVoiceText(), msgType.getVoiceValidityPeriod());
                        sendToPhone = true;
                }
            }
/*
            boolean sendToEmail = false;
            if (msg.getEmailAddress() != null) {

                //if ("Y".equalsIgnoreCase(phGrant.getAcceptVoice())){
                if (msgType.isAcceptEmail()) {
                    advMsg.addEmail(msg.getEmailText(), msg.getEmailSubject());
                    sendToEmail = true;
                }
                //}
            }
*/
            Destination destination = new Destination(msg.getExternalId(), (sendToPhone ? msg.getPhoneNumber() : null), /*(sendToEmail ? msg.getEmailAddress() : null)*/ null);
            advMsg.addDestination(destination);

            Report report = null;
            if (ims == null) ims = new InfobipMessageGateway();

            MessagesResponse sendMessagesResult = ims.sendAdvancedMessage(advMsg);

            boolean processMessage = true;
            if (sendMessagesResult.getError() == null){

                if (sendMessagesResult.getMessages().size() > 0) {


                    Status status = sendMessagesResult.getMessages().get(0).getStatus();
                    report = new Report(
                            msg.getId(),
                            status.getName(),
                            status.getGroupName(),
                            status.getDescription()
                    );
                }
            } else {
                if (sendMessagesResult.getRequestError().getErrorType().equals("ERROR")) {
                    /* Некоторые ошибки должны приводить к повторному запуску отправки! */
                    report = new Report(
                            msg.getId(),
                            "ERROR",
                            "ERROR",
                            "RequestError: " + sendMessagesResult.getError()
                    );
                } else {
                    processMessage = false;
                }
            }

            if (processMessage) {

                signalDeliveryProcessor = true;

                sessionForTransactions.getTransaction().begin();
                try {
                    // Удаляем сообщение из очереди рассылки
                    qmDAO.delete(qmsg);

                    //Добавляем отчёт об отправке
                    reportDAO.add(report);

                    sessionForTransactions.getTransaction().commit();
                } catch (JDBCException e) {
                    sessionForTransactions.getTransaction().rollback();
                }
            }
        }
        results.close();

        /* Обрабатываем пакетные сообщения */

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
}
