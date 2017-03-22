package com.bankir.mgs;


import com.bankir.mgs.hibernate.model.Message;
import com.bankir.mgs.hibernate.model.MessageType;
import com.bankir.mgs.hibernate.model.QueuedMessage;
import com.bankir.mgs.hibernate.model.Scenario;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class QueueProcessor  extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(QueueProcessor.class);
    private static QueueProcessor qp;
    private static MessageType defaultMessageType = new MessageType();

    private boolean breakeWithException = false;
    private Exception breakeException;

    public static synchronized QueueProcessor getInstance(){
        if (qp ==null) qp = new QueueProcessor();
        return qp;
    }

    /* Обработка очереди сообщений */
    protected void process() throws Exception {
        int MAX_MESSAGE_QUEUE = 1000;

        boolean signalDeliveryProcessor = false;

        //InfobipMessageGateway ims = null;

        StatelessSession sessionForQueries = Config.getHibernateSessionFactory().openStatelessSession();
        //StatelessSession sessionForTransactions = Config.getHibernateSessionFactory().openStatelessSession();

        /*
        MessageDAO msgDAO = new MessageDAO(sessionForQueries);
        QueuedMessageDAO qmDAO = new QueuedMessageDAO(sessionForTransactions);
        ReportDAO reportDAO = new ReportDAO(sessionForTransactions);
        */

        List<QueueMessagesHandler> msgHandlers = new ArrayList<>();


        BlockingQueue<QueueMessagesHandler.MessageData> msgQueue = new ArrayBlockingQueue<>(MAX_MESSAGE_QUEUE);

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

        Observer handlerObserver = (o, arg) -> {
            //Если ошибка коннекта с инфобипом
            if (arg instanceof InfobipMessageGateway.RequestErrorException){
                InfobipMessageGateway.RequestErrorException requestErrorException = (InfobipMessageGateway.RequestErrorException) arg;
                if (   requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.URL_ERROR)
                        ||requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.PROXY_ERROR)
                        /*||requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.CONNECTION_ERROR)*/
                   ){

                    msgQueue.clear();
                    //прерываем цикл по Result
                    breakeWithException = true;
                    breakeException = requestErrorException;
                }

                logger.error("Send Message Error: "+ requestErrorException.getMessage(), requestErrorException);
            }
        };

        int counter = 0;

        //try{

        while (!breakeWithException && results.next()) {
            counter=counter+1;
            if (  counter==1
                ||counter==1000
                ||counter==2000
                ||counter==3000
                ||counter==4000
            ){
                QueueMessagesHandler msgHandler = new QueueMessagesHandler(msgQueue, msgHandlers.size()+1);
                msgHandler.addObserver(handlerObserver);
                msgHandlers.add(msgHandler);
            }

            QueueMessagesHandler.MessageData msgData = new QueueMessagesHandler.MessageData(
                    (Message) results.get(0),
                    (Scenario) results.get(3),
                    (results.get(1)==null?defaultMessageType:(MessageType) results.get(1)),
                    (QueuedMessage) results.get(2)
            );

            msgQueue.add(msgData);
            signalDeliveryProcessor = true;

        }

        msgQueue.add(QueueMessagesHandler.DONE);


        results.close();
        sessionForQueries.close();

        // Уменьшим период запроса отчета до минимума
        // Сигналить нет смысла, т.к. сообщения ещё могут быть не отправлены, нужно некотрое время
        if (signalDeliveryProcessor){
            DeliveryReportProcessor drp = DeliveryReportProcessor.getInstance();
            drp.setSleepTime(DeliveryReportProcessor.MIN_SLEEP_TIME);
        }

        if (breakeWithException){
            throw breakeException;
        }

    }
}
