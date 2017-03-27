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

import java.util.Observer;
import java.util.concurrent.*;


public class QueueProcessor  extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(QueueProcessor.class);
    private static QueueProcessor qp;
    private static MessageType defaultMessageType = new MessageType();

    private volatile boolean breakeWithException = false;
    private volatile boolean breake = false;
    private volatile Exception breakeException;

    public static synchronized QueueProcessor getInstance(){
        if (qp ==null) qp = new QueueProcessor();
        return qp;
    }

    private synchronized void breakeWithException(Exception breakeException){
        this.breake = this.breakeWithException = true;
        this.breakeException = breakeException;
    }

    private synchronized void breake(){
        this.breake = true;
    }
    /* Обработка очереди сообщений */
    protected void process() throws Exception {
        int MAX_MESSAGE_QUEUE = 1000;
        boolean signalDeliveryProcessor = false;
        BlockingQueue<QueueMessagesHandler.MessageData> msgQueue = new ArrayBlockingQueue<>(MAX_MESSAGE_QUEUE);

        StatelessSession sessionForQueries = Config.getHibernateSessionFactory().openStatelessSession();
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

        Observer errorObserver = (o, arg) -> {
            InfobipMessageGateway.RequestErrorException requestErrorException = (InfobipMessageGateway.RequestErrorException) arg;
            //Очищаем очередь сообщений
            msgQueue.clear();
            // Для ошибок URL_ERROR и PROXY_ERROR останавливаем процесс, т.к. у нас неправильные настройки
            if (   requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.URL_ERROR)
                    ||requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.PROXY_ERROR)
                    /*||requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.CONNECTION_ERROR)*/
               ){
                //прерываем цикл по Result
                breakeWithException(requestErrorException);
            } else{
                breake();
            }
        };

        int threadsCount = 5;
        ExecutorService es = Executors.newFixedThreadPool(threadsCount);
        for (int i =0;i<threadsCount;i++){
            es.execute(new QueueMessagesHandler(msgQueue, i+1, errorObserver));
        }

        while (!breake && results.next()) {
            //counter=counter+1;
            QueueMessagesHandler.MessageData msgData = new QueueMessagesHandler.MessageData(
                    (Message) results.get(0),
                    (Scenario) results.get(3),
                    (results.get(1)==null?defaultMessageType:(MessageType) results.get(1)),
                    (QueuedMessage) results.get(2)
            );

            msgQueue.put(msgData);
        }
        //Сигнал на завершение
        msgQueue.put(QueueMessagesHandler.DONE);

        results.close();
        sessionForQueries.close();

        //Ждём завершения всех потоков
        es.shutdown();
        es.awaitTermination(0, TimeUnit.SECONDS);

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
