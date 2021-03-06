package com.bankir.mgs;

/**
 * Created by bankir on 22.03.17.
 */
public class SSLConfig {
    private String keystorePath;
    private String keystorePassword;
    private String keyPassword;
    private boolean useSsl;
    private int port;

    private String[] excludeProtocols;
    private String[] excludeCipherSuites;

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public int getPort() {
        return port;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public String[] getExcludeProtocols() {
        return excludeProtocols;
    }

    public String[] getExcludeCipherSuites() {
        return excludeCipherSuites;
    }
}
