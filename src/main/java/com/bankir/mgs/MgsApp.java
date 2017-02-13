package com.bankir.mgs;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;


public class MgsApp
{
    public static void main(String[] args) throws Exception {

        System.out.println(Character.isUnicodeIdentifierPart('.'));

        Logger logger = LoggerFactory.getLogger(MgsApp.class);
        logger.info("Start application");
        try {
            logger.debug("Get config settings");
            Settings settings = Config.getSettings();

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            context.addFilter(ResourcesAccessFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.INCLUDE));
            SessionHandler session = context.getSessionHandler();
            session.addEventListener(new AppSessionListener());

            logger.debug("Create servlet for static files");

        /* Создаём сервлет для статичных файлов */
            DefaultServlet defaultServlet = new DefaultServlet();
            ServletHolder defaultServletHolder = new ServletHolder(defaultServlet);

            logger.debug("Apply settings to servlet");
        /* Настройка параметров */
            for (Settings.Property property : settings.getDefaultServletProperties()) {
                defaultServletHolder.setInitParameter(property.getName(), property.getValue());
            }

        /* Прослушка сервлетом от корня веб сервера */
            context.addServlet(defaultServletHolder, "/*");

            logger.debug("Initialize jersey servlets");
        /* Интегрируем поддержку jersey сервлетов*/
            ResourceConfig resourceConfig = new ResourceConfig();
        /* Указываем в каких пакетах находятся сервлеты, поддерживаемые jersey */
            resourceConfig.packages("com.bankir.mgs.jersey, com.bankir.mgs.jersey.servlets");
        /* Поддержка загрузки файлов на сервер */
            //resourceConfig.register(MultiPartFeature.class);
        /* Поддержка SecurityContext в аннотациях сервлетов - PermitAll, AllowRoles и тп */
            //resourceConfig.register(RolesAllowedDynamicFeature.class);

            ServletContainer restServletContainer = new ServletContainer(resourceConfig);
            ServletHolder restHolder = new ServletHolder(restServletContainer);

        /* Прослушка сервлетом /json/* */
            context.addServlet(restHolder, Config.REST_PATH + "/*");

            if (Config.INFOBIP_AUTHORIZATION != null) {
            /* Стартуем поток для рассылки сообщений из очереди */
                QueueProcessor.getInstance().startProcessor("APP");
            /* Стартуем поток для получения отчетов о доставке */
                DeliveryReportProcessor.getInstance().startProcessor("APP");
            }

        /* если активировано, то включаем обработку файлов */
            if (settings.isUseFileProcessor()) {
                FileProcessor.getInstance().startProcessor("APP");
            }

/*

        String testSms = "{"+
                "  \"destinations\":[ \n" +
                "        { \n" +
                "          \"to\":{\n" +
                "              \"phoneNumber\": \"79203674776\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "  \"sms\": {\n" +
                "      \"text\": \"Test\"\n" +
                "  }\n" +
                "}";
        System.out.println(testSms);

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        OmniAdvancedMessage am = gson.fromJson(testSms, OmniAdvancedMessage.class);
        am.setScenarioId(settings.getDefaultScenarioKey());
        InfobipMessageGateway sender = new InfobipMessageGateway();
        sender.sendAdvancedMessage(am);
        sender.stop();
*/

        /* Создаём сервер Jetty на порту 8080 */
            logger.debug("Start jetty web server");

            Server jettyServer = new Server(settings.getPort());
            jettyServer.setHandler(context);
        /* Стартуем сервер */
            jettyServer.start();

            logger.info("Start complete");

        /* присоединяем к демону */
            jettyServer.join();
        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}
