package com.bankir.mgs;

import com.bankir.mgs.hibernate.dao.*;
import com.bankir.mgs.hibernate.model.*;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.ImsiRequest;
import com.bankir.mgs.infobip.model.ImsiResponse;
import com.bankir.mgs.infobip.model.InfobipObjects;
import com.bankir.mgs.infobip.model.MessagesResponse;
import org.hibernate.JDBCException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;


public class QueueMessagesHandler extends Observable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(QueueMessagesHandler.class);
    static final MessageData DONE= new MessageData();
    private InfobipMessageGateway ims;
    private StatelessSession session;
    private MessageDAO msgDAO;
    private QueuedMessageDAO qmDAO;
    private ReportDAO reportDAO;
    private int handlerId;
    private CountDownLatch cdl;

    private BlockingQueue<MessageData> queue;

    static class MessageData {
        private Message msg;
        private Scenario scenario;
        private MessageType msgType;
        private QueuedMessage qmsg;


        MessageData() {}

        MessageData(Message msg, Scenario scenario, MessageType msgType, QueuedMessage qmsg) {
            this.msg = msg;
            this.scenario = scenario;
            this.msgType = msgType;
            this.qmsg = qmsg;
        }

        Message getMessage() {
            return msg;
        }
        Scenario getScenario() {
            return scenario;
        }
        MessageType getMessageType() {
            return msgType;
        }
        QueuedMessage getQueuedMessage() {
            return qmsg;
        }
    }

    QueueMessagesHandler(CountDownLatch cdl, BlockingQueue<MessageData> queue, int id, Observer errorObserver) throws Exception {
        this.addObserver(errorObserver);;
        this.handlerId = id;
        this.cdl = cdl;
        this.queue = queue;
        logger.debug("START queueMessagesHandler id: {}, thread-id: {}", handlerId, Thread.currentThread().getId());
    }

    @Override
    public void run() {

        try {
            session = Config.getHibernateSessionFactory().openStatelessSession();
            msgDAO = new MessageDAO(session);
            qmDAO = new QueuedMessageDAO(session);
            reportDAO = new ReportDAO(session);

            ims = new InfobipMessageGateway();
            while (true) {
                MessageData msgData = queue.take();
                if (msgData == DONE) {
                    queue.add(DONE); //Чтобы другие потоки не зависли в ожидании данных в очереди
                    logger.debug("queueMessagesHandler id: {} get DONE message, thread-id: {}", handlerId, Thread.currentThread().getId());
                    break;
                }
                handleMessage(msgData);
            }
        } catch (InterruptedException ie) {
            logger.error("INTERRUPTED queueMessagesHandler id: {}, thread-id: {}", handlerId, Thread.currentThread().getId(), ie);
        } catch (JDBCConnectionException e) {
            this.setChanged();
            this.notifyObservers(e);
            logger.error("JDBCConnection EXCEPTION queueMessagesHandler id: {}, thread-id: {}, exception: {}", handlerId, Thread.currentThread().getId(), e.getSQLException().getMessage(), e);
        } catch (InfobipMessageGateway.RequestErrorException e) {
            this.setChanged();
            this.notifyObservers(e);
            logger.error("Request EXCEPTION queueMessagesHandler id: {}, thread-id: {}, exception: {}, type:{}", handlerId, Thread.currentThread().getId(), e.getMessage(), e.getType(), e);
        } finally {
            if (ims!=null) ims.stop();
            if (session!=null) try{ session.close();} catch (Exception ignored){}
        }


        ims = null;
        session = null;
        msgDAO = null;
        qmDAO = null;
        reportDAO = null;

        logger.debug("END queueMessagesHandler id: {}, thread-id: {}", handlerId, Thread.currentThread().getId());

        cdl.countDown();
    }

    private void handleMessage(MessageData msgData) throws InfobipMessageGateway.RequestErrorException {

        Message msg = msgData.getMessage();
        QueuedMessage qmsg = msgData.getQueuedMessage();
        MessageType msgType = msgData.getMessageType();
        Scenario scenario = msgData.getScenario();
/*
        if (msgType.isVerifyImsi()){
            if (imsiChanged(sessionForTransactions, ims, msg.getPhoneNumber())){
                smsText = viberText = parsecoText = voiceText = Config.getSettings().getImsiChangedMessage();
            }
        }
*/

        logger.debug("HANDLE message id: {}, queueMessagesHandler id: {}, thread-id: {}", msg.getExternalId(), handlerId, Thread.currentThread().getId());

        List<InfobipObjects.Destination> destinations = new ArrayList<>();
        destinations.add(new InfobipObjects.Destination(msg.getExternalId(), msg.getPhoneNumber()));

        MessagesResponse messagesResponse = ims.sendAdvancedMessage(
                new InfobipObjects.OmniAdvancedMessage(
                        scenario.getScenarioKey(),
                        null,
                        msg,
                        msgType,
                        destinations
                )
        );

        if (messagesResponse.getRequestError()!=null){
            if (messagesResponse.getRequestError().getServiceException()!=null) {
                logger.error("REQUEST ERROR: {}, {}", messagesResponse.getRequestError().getServiceException().getMessageId(), messagesResponse.getRequestError().getServiceException().getText());
            }
        }
        if (messagesResponse.getMessages()!=null) {
            if (messagesResponse.getMessages().size() > 0) {

                InfobipObjects.Status status;
                session.getTransaction().begin();

                try {
                    // Удаляем сообщение из очереди рассылки
                    qmDAO.delete(qmsg);

                    for (com.bankir.mgs.infobip.model.Message infobipMessage : messagesResponse.getMessages()) {
                        status = infobipMessage.getStatus();
                        Long msgId = null;
                        if (msg.getExternalId().equals(infobipMessage.getMessageId())) {
                            msgId = msg.getId();
                        } else {

                            Message msg2 = msgDAO.getByExternalId(infobipMessage.getMessageId());
                            if (msg2 != null) {
                                msgId = msg2.getId();
                            }
                        }

                        if (msgId != null) {
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
                    session.getTransaction().commit();

                } catch (JDBCException e) {
                    session.getTransaction().rollback();
                }

            }
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
