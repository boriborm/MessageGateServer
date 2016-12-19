package com.bankir.mgs;


import com.bankir.mgs.hibernate.dao.BulkDAO;
import com.bankir.mgs.hibernate.dao.UserDAO;
import com.bankir.mgs.hibernate.model.Bulk;
import com.bankir.mgs.jersey.model.MessageCreationRequestObject;
import com.bankir.mgs.jersey.model.MessageCreationResponseObject;
import org.hibernate.StatelessSession;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileProcessor extends AbstractProcessor {

    private static FileProcessor rp;

    public static final long MIN_SLEEP_TIME = 10000;
    public static final long MAX_SLEEP_TIME = 600000;

    private Settings.FilesProcessor filesProcessorSettings;
    private File folder;
    private File logFile;
    private File failurePath;
    private String defaultScenarioKey;
    private String defaultMessageType;
    private User user;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static synchronized FileProcessor getInstance(){
        if (rp ==null) rp = new FileProcessor();
        return rp;
    }

    @Override
    public synchronized void startProcessor() {

        filesProcessorSettings = Config.getSettings().getFilesProcessor();
        folder = new File (filesProcessorSettings.getPath());
        File logPath = new File(filesProcessorSettings.getLogPath());
        if (!logPath.exists()){
            logPath.mkdir();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String logFileName = "log-" + sdf.format(new Date()) +".log";
        logFile = new File(logPath, logFileName);
        failurePath = new File(filesProcessorSettings.getFailurePath());
        if (!failurePath.exists()){
            failurePath.mkdir();
        }

        defaultScenarioKey = Config.getSettings().getDefaultScenarioKey();
        defaultMessageType = Config.DEFAULT_MESSAGE_TYPE;

        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        UserDAO udao = new UserDAO(session);
        com.bankir.mgs.hibernate.model.User usr = udao.getByLogin(filesProcessorSettings.getUser());

        user = new User(usr.getId(), usr.getUserName(), usr.getRoles());

        session.close();

        if (folder==null||!(folder.exists()&&folder.isDirectory())){
            this.setStatus("Некорректный путь в настройках процессора");
            return;
        }

        if (user==null){
            this.setStatus("Несуществующий пользователь в настройках процессора");
            return;
        }

        super.startProcessor();
    }

    private void reloadSettings(){

    }

    /* Обработка */
    @Override
    protected void process() throws Exception {

        String scenarioKey;
        String messageType;
        boolean toSms;
        boolean toViber;
        boolean toParseco;
        boolean toVoice;

        String currentDateTime = sdf.format(new Date());
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        BufferedWriter logWriter  = new BufferedWriter(new FileWriter(logFile, true));

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()){


                FileInputStream fstream = new FileInputStream(fileEntry);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream, filesProcessorSettings.getCharset()));

                String strLine;
                int counter = 0;
                int fail = 0;
                int success = 0;
                MessageCreationRequestObject mreq= new MessageCreationRequestObject();

                session.getTransaction().begin();

                BulkDAO bulkDao = new BulkDAO(session);
                Bulk bulk = new Bulk(currentDateTime+" file "+ fileEntry.getName());
                bulkDao.add(bulk);

                session.getTransaction().commit();

                writeToLogWithTime(logWriter, "Обработка файла " + fileEntry.getName());

                while ((strLine = br.readLine()) != null) {

                    // Print the content on the console
                    String[] lineData = strLine.split(";");
                    /*
                    lineData[0] - ключ сценария, если пусто, то берется ключ по умолчанию
                    lineData[1] - тип сообщения, если пусто, то берется тип по умолчанию
                    lineData[2] - номер телефона
                    lineData[3] - Сообщение
                    lineData[4] - Каналы для отправки - "1111", где первая цифра - это SMS, вторая - Viber, третья - Parseco, четвертая - Voice
                    */

                    if (lineData.length ==5) {

                        if (counter % MessageGenerator.MAX_MESSAGES==0){
                            if (mreq.getMessages().size()>0) {
                                MessageCreationResponseObject mresp = MessageGenerator.Generate(session, mreq, user, bulk.getId());
                                fail += mresp.getFailedMessages().size();
                                success += mresp.getSuccessMessages().size();
                                writeLog(logWriter, mresp);
                                mreq = new MessageCreationRequestObject();
                            }
                        }

                        /* Инициализируем переменные */
                        if (lineData[0] != null && lineData[0].length() > 0) scenarioKey = lineData[0];
                        else scenarioKey = defaultScenarioKey;

                        if (lineData[1] != null && lineData[1].length() > 0) messageType = lineData[1];
                        else messageType = defaultMessageType;

                        toSms = toViber = toParseco = toVoice = false;

                        if (lineData[4] != null && lineData[4].length() > 0) {
                            toSms = getBool(lineData[4],0);
                            toViber = getBool(lineData[4],1);;
                            toParseco = getBool(lineData[4],2);;
                            toVoice = getBool(lineData[4],3);;
                        }

                        MessageCreationRequestObject.Message message = new MessageCreationRequestObject.Message(
                                Integer.toString(counter),
                                scenarioKey,
                                messageType,
                                lineData[2],
                                lineData[3],
                                toSms,
                                toViber,
                                toParseco,
                                toVoice
                        );
                        mreq.getMessages().add(message);
                    } else {
                        writeToLog(logWriter, "Ошибка формата строки: cтрока " + counter);
                        fail++;
                    }

                    counter++;

                }
                br.close();

                if (mreq.getMessages().size()>0) {
                    MessageCreationResponseObject mresp = MessageGenerator.Generate(session, mreq, user, bulk.getId());
                    fail += mresp.getFailedMessages().size();
                    success += mresp.getSuccessMessages().size();
                    writeLog(logWriter, mresp);
                }

                // Если есть сообщения, то создаем очередь для рассылки
                if (success>0){
                    MessageGenerator.CreateBulkQueue(session, bulk.getId());
                }

                writeToLogWithTime(logWriter,"Обработка файла завершена");
                if (fail==0)
                    fileEntry.delete();
                else
                    fileEntry.renameTo(new File (failurePath, fileEntry.getName()));
            }
        }

        logWriter.close();
        session.close();
    }

    private boolean getBool(String str, int pos){
        boolean ret = false;

        if(str.length()>pos){
            ret = "1".equals(str.substring(pos,pos));
        }

        return ret;
    }
    private void writeLog(BufferedWriter writer, MessageCreationResponseObject mresp){
        /* Выводим сведения о количестве успешно созданных сообщениях и обо всех отбракованных сообщениях */
        writeToLog(writer,"Обработка блока завершена: ");
        writeToLog(writer,"   Успешно - " + mresp.getSuccessMessages().size());
        writeToLog(writer,"   Ошибок - " + mresp.getFailedMessages().size());
        for (MessageCreationResponseObject.FailedMessage msg : mresp.getFailedMessages()) {
            writeToLog(writer, "   Ошибка в строке " + msg.getMessageId()+": "+msg.getError());
        }
    }


    private static void writeToLogWithTime(BufferedWriter writer, String str){
        try {
            writer.write(sdf.format(new Date())+" "+str);
            writer.newLine();
        } catch (IOException e) {
        }
    }
    private static void writeToLog(BufferedWriter writer, String str){
        try {
            writer.write("                      "+str);
            writer.newLine();
        } catch (IOException e) {
        }
    }

}
