package com.bankir.mgs;

import java.util.List;

public class Settings {

    private int port;
    private String defaultScenarioKey;
    private String messageIdPrefix;
    private boolean useProxy;
    private boolean useFileProcessor;
    private String rootDir;
    private String tmpDir;
    private String adminPath;
    private String opersPath;
    private int sessionTimeout;
    private String imsiChangedMessage;
    private String defaultMessageType;
    private Infobip infobip;
    private Proxy proxy;
    private SSLConfig sslConfig;
    private FilesProcessor filesProcessor;
    private QueueProcessorConfig queueProcessor;
    private DeliveryReportProcessorConfig deliveryReportProcessor;
    private List<Property> hibernateProperties;
    private List<Property> defaultServletProperties;
    private Notification stopProcessNotification;

    int getSessionTimeout() {
        return sessionTimeout;
    }
    String getAdminPath() {
        return adminPath;
    }
    String getOpersPath() {
        return opersPath;
    }

    public static class Property{
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    static class QueueProcessorConfig {
        private int maxMessageHandlerThreads;
        private int sleepTimeSeconds;

        int getMaxMessageHandlerThreads() {
            return maxMessageHandlerThreads;
        }

        long getSleepTime() {
            return sleepTimeSeconds * 1000;
        }
    }

    static class DeliveryReportProcessorConfig {
        private int maxSleepTimeSeconds;
        private int minSleepTimeSeconds;

        long getMaxSleepTime() {
            return maxSleepTimeSeconds * 1000;
        }
        long getMinSleepTime() {
            return minSleepTimeSeconds * 1000;
        }
    }

    static class FilesProcessor{

        private String path;
        private String charset;
        private String logPath;
        private String failurePath;
        private String user;
        public String getPath() {
            return path;
        }
        public String getCharset() {return charset;}

        String getLogPath() {
            return logPath;
        }

        String getFailurePath() {
            return failurePath;
        }

        public String getUser() {
            return user;
        }
    }

    public static class Infobip{
        private String sendMessageUrl;
        private String imsiUrl;
        private String sendSimpleMessageUrl;
        private String scenariosUrl;
        private String reportsUrl;
        private String login;
        private String password;

        public String getSendMessageUrl() {
            return sendMessageUrl;
        }

        public String getSendSimpleMessageUrl() {
            return sendSimpleMessageUrl;
        }


        public String getImsiUrl() {
            return imsiUrl;
        }

        public String getScenariosUrl() {
            return scenariosUrl;
        }

        public String getReportsUrl() {
            return reportsUrl;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class Proxy{
        private String url;
        private String realm;
        private String login;
        private String password;

        public String getUrl() {
            return url;
        }

        public String getRealm() {
            return realm;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class Notification{
        private String[] phones;
        String[] getPhones() {
            return phones;
        }
    }

    int getPort() {
        return (this.port==0?8080:this.port);
    }

    List<Property> getHibernateProperties(){return this.hibernateProperties;}

    List<Property> getDefaultServletProperties() {
        return defaultServletProperties;
    }

    public String getDefaultScenarioKey() {
        return defaultScenarioKey;
    }
    public boolean isUseProxy() {
        return useProxy;
    }
    String getMessageIdPrefix() {
        return messageIdPrefix;
    }

    public Infobip getInfobip() {
        return infobip;
    }

    public Proxy getProxy() {
        return proxy;
    }

    FilesProcessor getFilesProcessor() {
        return filesProcessor;
    }

    boolean isUseFileProcessor() {
        return useFileProcessor;
    }

    public String getImsiChangedMessage() {
        return imsiChangedMessage;
    }

    public String getDefaultMessageType() {
        return defaultMessageType;
    }

    SSLConfig getSslConfig() {
        return sslConfig;
    }

    QueueProcessorConfig getQueueProcessorConfig() {
        return queueProcessor;
    }

    DeliveryReportProcessorConfig getDeliveryReportProcessorConfig() {
        return deliveryReportProcessor;
    }

    Notification getStopProcessNotificationConfig() {
        return stopProcessNotification;
    }

    String getRootDir() {
        return rootDir;
    }

    String getTmpDir() {
        return tmpDir;
    }
}
