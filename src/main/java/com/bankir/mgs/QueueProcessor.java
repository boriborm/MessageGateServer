package com.bankir.mgs;


import com.bankir.mgs.hibernate.model.Message;
import com.bankir.mgs.hibernate.model.MessageType;
import com.bankir.mgs.hibernate.model.QueuedMessage;
import com.bankir.mgs.hibernate.model.Scenario;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observer;
import java.util.concurrent.*;


public class QueueProcessor  extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(QueueProcessor.class);
    private static QueueProcessor qp;
    private static MessageType defaultMessageType = new MessageType();

    private static int MAXIMUM_MESSAGEHANDLER_THREADS = Config.getSettings().getQueueProcessorConfig().getMaxMessageHandlerThreads();

    private volatile boolean breakeWithException = false;
    private volatile boolean breake = false;
    private volatile Exception breakeException;

    public static synchronized QueueProcessor getInstance(){

        if (qp ==null){
            logger.debug("create QueueProcessor. MAXIMUM_MESSAGEHANDLER_THREADS: {}, SLEEP_TIME (ms): {}",MAXIMUM_MESSAGEHANDLER_THREADS, Config.getSettings().getQueueProcessorConfig().getSleepTime());
            qp = new QueueProcessor();
            qp.setSleepTime(Config.getSettings().getQueueProcessorConfig().getSleepTime());
        }

        return qp;
    }

    private synchronized void breakeWithException(Exception breakeException){
        logger.debug("BREAK WITH EXCEPTION:", breakeException);
        this.breake = this.breakeWithException = true;
        this.breakeException = breakeException;
    }

    private synchronized void breake(){
        logger.debug("BREAK");
        this.breake = true;
    }
    /* Обработка очереди сообщений */
    protected void process() throws Exception {
        int MAX_MESSAGE_QUEUE = 1000;
        boolean signalDeliveryProcessor = false;

        breake = false; // Инициализируем перед обработкой!!!
        breakeException = null;

        BlockingQueue<QueueMessagesHandler.MessageData> msgQueue = new ArrayBlockingQueue<>(MAX_MESSAGE_QUEUE);

        StatelessSession session = null;
        ScrollableResults results = null;
        try{

            session = Config.getHibernateSessionFactory().openStatelessSession();

            Query countQuery = session.createQuery("SELECT count (qm.id) FROM QueuedMessage qm");
            Long countMessages = (Long) countQuery.uniqueResult();

            if (countMessages==0) return;

            int threadsCount = (int) (countMessages/1000);
            if (threadsCount==0) threadsCount=1;
            if (threadsCount>MAXIMUM_MESSAGEHANDLER_THREADS) threadsCount = MAXIMUM_MESSAGEHANDLER_THREADS;
            logger.debug("Start QUEUE processing. Count: {}, treads count: {}", countMessages, threadsCount);

            CountDownLatch cdl = new CountDownLatch(threadsCount);

            Observer errorObserver = (o, arg) -> {
                msgQueue.clear();
                msgQueue.add(QueueMessagesHandler.DONE);
                if (arg instanceof InfobipMessageGateway.RequestErrorException) {

                    InfobipMessageGateway.RequestErrorException requestErrorException = (InfobipMessageGateway.RequestErrorException) arg;
                    logger.debug("RequestErrorException. Error: {}", requestErrorException.getType());
                    //Очищаем очередь сообщений
                    // Для ошибок URL_ERROR и PROXY_ERROR останавливаем процесс, т.к. у нас неправильные настройки
                    if (requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.URL_ERROR)
                            || requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.PROXY_ERROR)
                        /*||requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.CONNECTION_ERROR)*/
                            ) {
                        //прерываем цикл по Result
                        breakeWithException(requestErrorException);
                    } else {
                        breake();
                    }
                }

                if (arg instanceof JDBCConnectionException){
                    JDBCConnectionException e = (JDBCConnectionException) arg;
                    logger.debug("JDBCConnectionException. Error: {}", e.getSQLException() );
                    breake();
                }
            };


            ExecutorService es = Executors.newFixedThreadPool(threadsCount);
            for (int i =0;i<threadsCount;i++){
                es.execute(new QueueMessagesHandler(cdl, msgQueue, i+1, errorObserver));
            }

            /* Обрабатываем одиночные сообщения */

            Query query = session.createQuery(
                    "FROM Message m " +
                            "JOIN MessageType mt ON m.typeId=mt.typeId " +
                            "JOIN QueuedMessage qm ON m.id=qm.messageId " +
                            "JOIN Scenario s ON m.scenarioId=s.id "
            );
            query.setFetchSize(1000);
            query.setReadOnly(true);
            results = query.scroll(ScrollMode.FORWARD_ONLY);
            long counter = 0;
            while (!breake && results.next()) {
                counter=counter+1;
                QueueMessagesHandler.MessageData msgData = new QueueMessagesHandler.MessageData(
                        (Message) results.get(0),
                        (Scenario) results.get(3),
                        (results.get(1)==null?defaultMessageType:(MessageType) results.get(1)),
                        (QueuedMessage) results.get(2)
                );
                logger.debug("PUT message to queue, id: {}", ((Message) results.get(0)).getExternalId());
                msgQueue.put(msgData);
                signalDeliveryProcessor = true;
            }

            logger.debug("QUEUE count: {}", counter);

            //Сигнал на завершение
            logger.debug("Put DONE message in queue");
            msgQueue.put(QueueMessagesHandler.DONE);

            results.close();
            session.close();

            //Ждём завершения всех потоков
            cdl.await();
            logger.debug("Stop QUEUE processing. queue size: {}", msgQueue.size());

            // Уменьшим период запроса отчета до минимума
            // Сигналить нет смысла, т.к. сообщения ещё могут быть не отправлены, нужно некотрое время
            if (signalDeliveryProcessor){
                DeliveryReportProcessor drp = DeliveryReportProcessor.getInstance();
                drp.setSleepTime(DeliveryReportProcessor.MIN_SLEEP_TIME);
            }
            logger.debug("breakeWithException is {}", breakeWithException);
            if (breakeWithException){
                throw breakeException;
            }
//        } catch(JDBCConnectionException|GenericJDBCException ignored){
            //Если ошибка соединения, то ничего не делаем, просто выходим из процесса.
        }
        finally{
            if (results!=null){
                try { results.close();} catch (Exception ignored){}
            }
            if (session!=null){
                try { session.close();} catch (Exception ignored){}
            }
        }

    }
}
