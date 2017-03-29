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
