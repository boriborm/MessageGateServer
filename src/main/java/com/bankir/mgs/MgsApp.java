package com.bankir.mgs;


import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.io.File;
import java.io.FileNotFoundException;
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

            Server jettyServer = new Server();

            jettyServer.setStopAtShutdown(true);
            jettyServer.setStopTimeout(600000);

            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setOutputBufferSize(32768);

            ServerConnector http = new ServerConnector(jettyServer, new HttpConnectionFactory(http_config));
            http.setPort(settings.getPort());
            http.setIdleTimeout(30000);

            jettyServer.addConnector(http);

            if (settings.getSslConfig().isUseSsl()) {
                String keystorePath = settings.getSslConfig().getKeystorePath();
                File keystoreFile = new File(keystorePath);
                if (!keystoreFile.exists()) {
                    throw new FileNotFoundException(keystoreFile.getAbsolutePath());
                }

                SslContextFactory sslContextFactory = new SslContextFactory();

                if (settings.getSslConfig().getExcludeCipherSuites()!=null) sslContextFactory.setExcludeCipherSuites(settings.getSslConfig().getExcludeCipherSuites());
                if (settings.getSslConfig().getExcludeProtocols()!=null) sslContextFactory.setExcludeProtocols(settings.getSslConfig().getExcludeProtocols());

                sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
                sslContextFactory.setKeyStorePassword(settings.getSslConfig().getKeystorePassword());
                sslContextFactory.setKeyManagerPassword(settings.getSslConfig().getKeyPassword());


                HttpConfiguration https_config = new HttpConfiguration();
                https_config.setSecureScheme("https");
                https_config.setOutputBufferSize(32768);

                SecureRequestCustomizer src = new SecureRequestCustomizer();
                src.setStsMaxAge(2000);
                src.setStsIncludeSubDomains(true);
                https_config.addCustomizer(src);

                ServerConnector https = new ServerConnector(jettyServer,
                        new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(https_config));
                https.setPort(settings.getSslConfig().getPort());
                jettyServer.addConnector(https);
            }

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

            jettyServer.setHandler(context);
        /* Стартуем сервер */
            jettyServer.start();

            logger.info("Start web server complete");

        /* присоединяем к демону */
            jettyServer.join();
        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}
