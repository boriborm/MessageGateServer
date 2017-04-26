package com.bankir.mgs;


import com.bankir.mgs.hibernate.dao.MessageDAO;
import com.bankir.mgs.hibernate.dao.ReportDAO;
import com.bankir.mgs.hibernate.model.Message;
import com.bankir.mgs.hibernate.model.Report;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.DeliveryReport;
import com.bankir.mgs.infobip.model.InfobipObjects;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryReportProcessor extends AbstractProcessor {

    private static DeliveryReportProcessor rp;
    private static final Logger logger = LoggerFactory.getLogger(DeliveryReportProcessor.class);

    static final long MIN_SLEEP_TIME = Config.getSettings().getDeliveryReportProcessorConfig().getMinSleepTime();
    private static final long MAX_SLEEP_TIME = Config.getSettings().getDeliveryReportProcessorConfig().getMaxSleepTime();

    private static long SLEEP_STEP;
    public static synchronized DeliveryReportProcessor getInstance(){
        SLEEP_STEP = (MAX_SLEEP_TIME - MIN_SLEEP_TIME)/5;
        if (rp ==null) rp = new DeliveryReportProcessor();
        return rp;
    }

    /* Обработка */
    @Override
    protected void process() throws Exception {

        String messageId;
        String prevMessageId = null;
        //System.out.println("process deliveryReport " + (new Date()).toString());

        InfobipMessageGateway ims = null;
        StatelessSession sessionForTransactions = null;

        Message msg = null;
        try {
            ims = new InfobipMessageGateway();


            DeliveryReport deliveryReport = ims.getReport();


            if (deliveryReport.getRequestError()!=null){
                if (deliveryReport.getRequestError().getServiceException()!=null) {
                    logger.error("REQUEST ERROR: {}, {}", deliveryReport.getRequestError().getServiceException().getMessageId(), deliveryReport.getRequestError().getServiceException().getText());
                }
            }

            if (deliveryReport.getResults()==null) return;

            if (deliveryReport.getResults().size() > 0) {
                //Уменьшаем сон до минимума (10 сек)
                this.setSleepTime(MIN_SLEEP_TIME);
            } else { //Или постепенно растягиваем сон до 10 минут
                long sleepTime = this.getSleepTime();
                if (sleepTime < MAX_SLEEP_TIME)
                    this.setSleepTime(sleepTime + SLEEP_STEP);
            }



            /* Бежим по сообщениям и сохраняем их статусы в БД,
            *  перепривязываем текущий статус
            */
            sessionForTransactions = Config.getHibernateSessionFactory().openStatelessSession();

            MessageDAO msgDAO = new MessageDAO(sessionForTransactions);
            ReportDAO rptDAO = new ReportDAO(sessionForTransactions);


            for (InfobipObjects.Result message : deliveryReport.getResults()) {

                try {



                    messageId = message.getMessageId();

                    // Получаем запись о сообщении из БД в случае, если предыдущая запись не она же
                    // Один и тот же идентификатор в отчёте может быть для разных каналов.
                    if (!messageId.equals(prevMessageId)) {
                        msg = msgDAO.getByExternalId(messageId);
                        prevMessageId = messageId;
                    }

                    if (msg != null) {

                        Report report = new Report(
                                msg.getId(),
                                message.getStatus().getName(),
                                message.getStatus().getGroupName(),
                                message.getStatus().getDescription()
                        );
                        report.setChannel(message.getChannel());
                        report.setDoneAt(message.getDoneAt());
                        report.setSentAt(message.getSentAt());
                        report.setPricePerMessage(message.getPrice().getPricePerMessage());
                        report.setPriceCurrency(message.getPrice().getCurrency());
                        report.setMessageCount(message.getMessageCount());
                        report.setMccMnc(message.getMccMnc());

                        sessionForTransactions.getTransaction().begin();

                        rptDAO.add(report);
/* По идее тут надо сделать обработку статуса
   например, получатель не валидный,
   тогда можно добавить в phoneGrants с отключением всех каналов
* */
                        sessionForTransactions.getTransaction().commit();
                    }
                } catch (JDBCException e) {
                    logger.error("Error: " + e.getSQLException().getMessage(), e);
                    sessionForTransactions.getTransaction().rollback();
                    break;
                }
            }

        } catch (InfobipMessageGateway.RequestErrorException requestErrorException) {
            logger.error("Get report infobip error: " + requestErrorException.getMessage());
            // В случае ошибок URL_ERROR, PROXY_ERROR, AUTH_ERROR - неверные настройки сервера. Процесс получения репортов останавливается
            // до исправления

            if (requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.URL_ERROR)
                    || requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.PROXY_ERROR)
                    || requestErrorException.getType().equals(InfobipMessageGateway.ConnectionErrors.AUTH_ERROR)
                    ) {

                throw requestErrorException;
            }
        } finally{
            if (sessionForTransactions!=null){
                try{ sessionForTransactions.close();} catch(Exception ignored){}
            }
            if (ims!=null){
                try{ ims.stop();} catch(Exception ignored){}
            }
        }
    }
}
