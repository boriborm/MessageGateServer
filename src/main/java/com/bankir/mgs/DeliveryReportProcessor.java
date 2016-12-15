package com.bankir.mgs;


import com.bankir.mgs.hibernate.dao.MessageDAO;
import com.bankir.mgs.hibernate.dao.ReportDAO;
import com.bankir.mgs.hibernate.model.Message;
import com.bankir.mgs.hibernate.model.Report;
import com.bankir.mgs.infobip.InfobipMessageGateway;
import com.bankir.mgs.infobip.model.DeliveryReport;
import com.bankir.mgs.infobip.model.Result;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class DeliveryReportProcessor extends AbstractProcessor {

    private static DeliveryReportProcessor rp;

    public static final long MIN_SLEEP_TIME = 10000;
    public static final long MAX_SLEEP_TIME = 600000;

    public static synchronized DeliveryReportProcessor getInstance(){
        if (rp ==null) rp = new DeliveryReportProcessor();
        return rp;
    }

    /* Обработка */
    @Override
    protected void process() throws Exception {

        String messageId;
        String prevMessageId = null;
        //System.out.println("process deliveryReport " + (new Date()).toString());

        InfobipMessageGateway ims = new InfobipMessageGateway();


        StatelessSession sessionForTransactions = Config.getHibernateSessionFactory().openStatelessSession();
        MessageDAO msgDAO = new MessageDAO(sessionForTransactions);
        ReportDAO rptDAO = new ReportDAO(sessionForTransactions);

        Message msg = null;

        DeliveryReport deliveryReport = ims.getReport();
        if (deliveryReport!=null){


            if (deliveryReport.getResults().size()>0){
                //Уменьшаем сон до минимума (10 сек)
                this.setSleepTime(MIN_SLEEP_TIME);
            } else { //Или постепенно растягиваем сон до 10 минут
                long sleepTime = this.getSleepTime();
                if (sleepTime<MAX_SLEEP_TIME)
                this.setSleepTime(sleepTime+10000);
            }

            /* Бежим по сообщениям и сохраняем их статусы в БД,
            *  перепривязываем текущий статус
            */

            sessionForTransactions.getTransaction().begin();
            for (Result message:deliveryReport.getResults()){

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

                        rptDAO.add(report);
/* По идее тут надо сделать обработку статуса
   например, получатель не валидный,
   тогда можно добавить в phoneGrants с отключением всех каналов
* */
                        sessionForTransactions.getTransaction().commit();
                    }
                } catch (JDBCException e){
                    sessionForTransactions.getTransaction().rollback();
                    e.printStackTrace();
                }
            }

        }
        ims.stop();
        /* Обрабатываем пакетные сообщения */
        sessionForTransactions.close();
        ims.stop();
    }
}
