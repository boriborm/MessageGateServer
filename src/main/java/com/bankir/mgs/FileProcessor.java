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
    private String defaultChannels = "S";
    private String defaultMessageType;
    private User user;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static synchronized FileProcessor getInstance(){
        if (rp ==null) rp = new FileProcessor();
        return rp;
    }

    @Override
    public synchronized void startProcessor(String userLogin) {

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

//        defaultScenarioKey = Config.getSettings().getDefaultScenarioKey();
        defaultMessageType = Config.getSettings().getDefaultMessageType();

        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        UserDAO udao = new UserDAO(session);
        com.bankir.mgs.hibernate.model.User usr = udao.getByLogin(filesProcessorSettings.getUser());

        user = new User(usr.getId(), usr.getLogin(), usr.getUserName(), usr.getRoles());

        session.close();

        if (folder==null||!(folder.exists()&&folder.isDirectory())){
            this.setErrorStatus(new Date(), "Некорректный путь в настройках процессора", null);
            return;
        }

        if (user==null){
            this.setErrorStatus(new Date(), "Несуществующий пользователь в настройках процессора", null);
            return;
        }

        super.startProcessor(userLogin);
    }

    /* Обработка */
    @Override
    protected void process() throws Exception {

        String channels;
        String messageType;
        String bulkDescription;

        String currentDateTime = sdf.format(new Date());
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();

        BufferedWriter logWriter  = new BufferedWriter(new FileWriter(logFile, true));

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()){


                FileInputStream fstream = new FileInputStream(fileEntry);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream, filesProcessorSettings.getCharset()));

                int counter = 0;
                int fail = 0;
                int success = 0;

                String strLine = br.readLine();
                if (strLine != null) {
                    String[] lineHeader = strLine.split(";");

                    //lineHeader[0] - тип сообщения, если пусто, то берется тип по умолчанию
                    //lineHeader[2] - Описание рассылки
                    //lineHeader[1] - Каналы для отправки - "1111", где первая цифра - это SMS, вторая - Viber, третья - Parseco, четвертая - Voice
                    if (lineHeader[0] != null && lineHeader[0].length() > 0) messageType = lineHeader[0];
                    else messageType = defaultMessageType;

                    if (lineHeader[1] != null && lineHeader[1].length() > 0) channels = lineHeader[1];
                    else channels = defaultChannels;


                    if (lineHeader[2] != null && lineHeader[2].length() > 0) bulkDescription = lineHeader[2];
                    else bulkDescription = "file " + fileEntry.getName();

                    bulkDescription = currentDateTime + " "+ bulkDescription;


                    session.getTransaction().begin();

                    BulkDAO bulkDao = new BulkDAO(session);
                    Bulk bulk = new Bulk(bulkDescription);
                    bulkDao.add(bulk);

                    session.getTransaction().commit();

                    writeToLogWithTime(logWriter, "Обработка файла " + fileEntry.getName());

                    MessageCreationRequestObject mreq = new MessageCreationRequestObject(
                            channels,
                            messageType
                    );
                    MessageGenerator mg = new MessageGenerator(user, session, bulk.getId());

                    while ((strLine = br.readLine()) != null) {

                        // Print the content on the console
                        String[] lineData = strLine.split(";");
                        /*
                        lineData[0] - номер телефона
                        lineData[1] - сообщение
                        */

                        if (lineData.length == 2) {

                            if (counter % MessageGenerator.MAX_MESSAGES == 0) {
                                if (mreq.getMessages().size() > 0) {
                                    MessageCreationResponseObject mresp = mg.generate(mreq);
                                    if (mresp.isSuccess()) {
                                        fail += mresp.getFailedMessages().size();
                                        success += mresp.getSuccessMessages().size();
                                        writeLog(logWriter, mresp);
                                        mreq = new MessageCreationRequestObject(
                                                channels,
                                                messageType
                                        );
                                    } else{
                                        fail+=mreq.getMessages().size();
                                        writeToLog(logWriter,"Ошибка обработки блока: "+mresp.getError());
                                    }
                                }
                            }

                            /* Инициализируем переменные */
                            MessageCreationRequestObject.Message message = new MessageCreationRequestObject.Message(
                                    Integer.toString(counter),
                                    lineData[0],
                                    lineData[1]
                            );
                            mreq.getMessages().add(message);

                        } else {
                            writeToLog(logWriter, "Ошибка формата строки: cтрока " + counter);
                            fail++;
                        }
                        counter++;

                    }

                    if (mreq.getMessages().size()>0) {
                        MessageCreationResponseObject mresp = mg.generate(mreq);
                        if (mresp.isSuccess()) {
                            fail += mresp.getFailedMessages().size();
                            success += mresp.getSuccessMessages().size();
                            writeLog(logWriter, mresp);
                        } else{
                            fail+=mreq.getMessages().size();
                            writeToLog(logWriter,"Ошибка обработки блока: "+mresp.getError());
                        }
                    }
                }
                br.close();

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
        } catch (IOException ignored) {
        }
    }
    private static void writeToLog(BufferedWriter writer, String str){
        try {
            writer.write("                      "+str);
            writer.newLine();
        } catch (IOException ignored) {
        }
    }

}
