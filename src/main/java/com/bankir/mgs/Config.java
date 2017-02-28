package com.bankir.mgs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.client.HttpProxy;
import org.hibernate.JDBCException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class Config {
    private static Settings settings;

    private static SessionFactory hibernateSessionFactory;
    private static HttpProxy proxy;
    public static final String MSG_FORBIDDEN = "Access denied";
    public static final String MSG_UNAUTHORIZED = "Unauthorized";
    public static final String REST_PATH = "/rest";
    private static final GsonBuilder gsonBuilder = new GsonBuilder();

    public static final com.bankir.mgs.User ANONYMOUS_USER = new com.bankir.mgs.User(-1L, "ANONYMOUS", "ANONYMOUS", null);

    public static String INFOBIP_AUTHORIZATION;
/*
    public class Property1{
        String name;
        String value;

    }
    public static class Settings1 {
        private int port = 8080;
        private List<Property> hibernateProperties;
        private List<Property> defaultServletProperties;
        private String defaultScenarioKey;
        private String infobipOmniAdvancedMessageUrl;
        private String infobipOmniSimpleMessageUrl;
        private String infobipOmniScenariosUrl;
        private String infobipOmniReportsUrl;
        private String infobipAuthorization;
        private String infobipLogin;
        private String messageIdPrefix = "MGS";
        private String proxyURI;
        private String proxyRealm;
        private String proxyLogin;
        private String proxyPass;
        private String useProxy = "N";
        private String adminUriMatch;
        private String authorizedUriMatch;
        private int sessionTimeout = 600;

        public String getLogin() {
            return infobipLogin;
        }

        public void setInfobipLogin(String infobipLogin) {
            this.infobipLogin = infobipLogin;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public String getAdminPath() {
            return adminUriMatch;
        }

        public String getOpersPath() {
            return authorizedUriMatch;
        }

        int getPort() {
            return this.port;
        }

        List<Property> getHibernateProperties(){return this.hibernateProperties;}

        List<Property> getDefaultServletProperties() {
            return defaultServletProperties;
        }

        public String getDefaultScenarioKey() {
            return defaultScenarioKey;
        }

        public void setDefaultScenarioKey(String defaultScenarioKey) {
            this.defaultScenarioKey = defaultScenarioKey;
        }

        public String getSendMessageUrl() {
            return infobipOmniAdvancedMessageUrl;
        }

        public void setInfobipOmniAdvancedMessageUrl(String infobipOmniAdvancedMessageUrl) {
            this.infobipOmniAdvancedMessageUrl = infobipOmniAdvancedMessageUrl;
        }

        public String getInfobipOmniSimpleMessageUrl() {
            return infobipOmniSimpleMessageUrl;
        }

        public void setInfobipOmniSimpleMessageUrl(String infobipOmniSimpleMessageUrl) {
            this.infobipOmniSimpleMessageUrl = infobipOmniSimpleMessageUrl;
        }

        public String getScenariosUrl() {
            return infobipOmniScenariosUrl;
        }

        public void setInfobipOmniScenariosUrl(String infobipOmniScenarioUrl) {
            this.infobipOmniScenariosUrl = infobipOmniScenarioUrl;
        }

        public String getAuthorizationHeader() {
            return infobipAuthorization;
        }

        public void setInfobipAuthorization(String infobipAuthorization) {
            this.infobipAuthorization = infobipAuthorization;
        }

        public String getReportsUrl() {
            return infobipOmniReportsUrl;
        }

        public void setInfobipOmniReportsUrl(String infobipOmniReportsUrl) {
            this.infobipOmniReportsUrl = infobipOmniReportsUrl;
        }

        public String getUrl() {
            return proxyURI;
        }

        public void setProxyURI(String proxyURI){
             this.proxyURI = proxyURI;
        }

        public String getLogin() {
            return proxyLogin;
        }

        public void setProxyLogin(String proxyLogin) {
            this.proxyLogin = proxyLogin;
        }

        public String getPassword() {
            return proxyPass;
        }

        public void setProxyPass(String proxyPass) {
            this.proxyPass = proxyPass;
        }

        public String getUseProxy() {
            return useProxy;
        }

        public void setUseProxy(String useProxy) {
            this.useProxy = useProxy;
        }

        public String getRealm() {
            return proxyRealm;
        }

        public void setProxyRealm(String proxyRealm) {
            this.proxyRealm = proxyRealm;
        }

        public String getMessageIdPrefix() {
            return messageIdPrefix;
        }

        public void setMessageIdPrefix(String messageIdPrefix) {
            this.messageIdPrefix = messageIdPrefix;
        }
    }

*/
    static {
        try {
            /* Настраиваем gsonBuilder */
            gsonBuilder.setDateFormat("yyyy-MM-dd")
                    .setPrettyPrinting();

            /* Считываем настройки из json файла в settings */
            Gson gson = gsonBuilder.create();

            settings = gson.fromJson(new InputStreamReader(new FileInputStream(System.getProperty("user.dir")+ File.separator+"settings.json")), Settings.class);
            configProxy();

            INFOBIP_AUTHORIZATION = BasicAuth.encode(
                settings.getInfobip().getLogin(),
                settings.getInfobip().getPassword()
            );

            /* Настраиваем Hibernate*/
            Configuration cfg = new Configuration();
            addPackageWithAnnotatedClasses(cfg, "com.bankir.mgs.hibernate.model");

            for (Settings.Property property:settings.getHibernateProperties()) {
                if (property!=null){
                    cfg.getProperties().setProperty(property.getName(), property.getValue());
                }
            }

            hibernateSessionFactory = cfg.buildSessionFactory();



        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        } catch (URISyntaxException e) {
            System.out.println("URI exception "+e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void addPackageWithAnnotatedClasses(Configuration cfg, String scannedPackage) throws IOException {

        String CLASS_FILE_SUFFIX = ".class";

        String scannedPath = scannedPackage.replace(".", "/")+"/";
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);

        if (scannedUrl == null) return;

        URLConnection connection = scannedUrl.openConnection();

                if (connection instanceof JarURLConnection) {
                    final JarFile jarFile = ((JarURLConnection) connection).getJarFile();
                    final Enumeration<JarEntry> entries = jarFile.entries();
                    String name;

                    for (JarEntry jarEntry; entries.hasMoreElements()
                            && ((jarEntry = entries.nextElement()) != null);) {
                        name = jarEntry.getName();

                        if (name.contains(CLASS_FILE_SUFFIX)) {
                            name = name.substring(0, name.length() - 6).replace('/', '.');

                            if (name.contains(scannedPackage)) {
                                try {
                                    cfg.addAnnotatedClass(Class.forName(name));
                                } catch (ClassNotFoundException ignore) {
                                }
                            }
                        }
                    }
                } else if (connection instanceof FileURLConnection) {
                    File scannedDir = new File(scannedUrl.getFile());
                    for (File file : scannedDir.listFiles()) {
                        String fileName = file.getName();
                        if (!file.isDirectory())
                            if (fileName.endsWith(CLASS_FILE_SUFFIX)) {
                                int endIndex = fileName.length() - CLASS_FILE_SUFFIX.length();
                                String className = scannedPackage+"."+fileName.substring(0, endIndex);
                                try {
                                    cfg.addAnnotatedClass(Class.forName(className));
                                } catch (ClassNotFoundException ignore) {
                                }
                            }
                    }
                }
    }


    public static synchronized void  reloadPreferences() throws JDBCException, URISyntaxException, FileNotFoundException {
            //loadSettingsFromDB();
        settings = new Gson().fromJson(new InputStreamReader(new FileInputStream(System.getProperty("user.dir")+ File.separator+"settings.json")), Settings.class);
        configProxy();
    }

    public static Settings getSettings(){
        return settings;
    }

    public static SessionFactory getHibernateSessionFactory(){return hibernateSessionFactory;}

    private static void configProxy() throws URISyntaxException {
        if (settings.isUseProxy()) {
            URI uri = new URI(settings.getProxy().getUrl());
            proxy = new HttpProxy(uri.getHost(),uri.getPort());
        } else proxy = null;
    }

    public static HttpProxy getProxy() {
        return proxy;
    }

    public static GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }
}
