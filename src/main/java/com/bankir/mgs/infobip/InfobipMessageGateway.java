package com.bankir.mgs.infobip;

import com.bankir.mgs.Config;
import com.bankir.mgs.Settings;
import com.bankir.mgs.infobip.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class InfobipMessageGateway {
    private static final Logger logger = LoggerFactory.getLogger(InfobipMessageGateway.class);

    //private String url;
    private HttpClient httpClient;
    private String authorization;
    private static final int  timeOut = 15;
    private static Gson gson;


    public enum ConnectionErrors  {UNKNOWN_ERROR, URL_ERROR, PROXY_ERROR, AUTH_ERROR, INTERRUPTED, CONNECTION_ERROR, TIMEOUT };

    public static class RequestErrorException extends Exception {
        private ConnectionErrors type;

        public RequestErrorException(String message, ConnectionErrors type) {
            super(message);
            this.type = type;
        }

        public ConnectionErrors getType() {
            return type;
        }
    }

    public InfobipMessageGateway() throws Exception {
        Settings settings = Config.getSettings();

        authorization = Config.INFOBIP_AUTHORIZATION;
        SslContextFactory sslContextFactory = new SslContextFactory();
        httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);

        if (settings.isUseProxy()) {
            // Proxy credentials.
            AuthenticationStore auth = httpClient.getAuthenticationStore();

            Settings.Proxy proxy = settings.getProxy();

            auth.addAuthentication(
                    new BasicAuthentication(
                            new URI(proxy.getUrl()),
                            proxy.getRealm(),
                            proxy.getLogin(),
                            proxy.getPassword()
                    )
            );
            ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
            proxyConfig.getProxies().add(Config.getProxy());
        }

        httpClient.start();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();

        logger.debug("Start Infobip gateway connection");
    }

    public void stop(){
        try {
            httpClient.stop();
            logger.debug("Stop Infobip gateway connection");
        } catch (Exception e) {
            logger.error("Error: "+e.getMessage(), e);
        }
    }

    public MessagesResponse sendAdvancedMessage(InfobipObjects.OmniAdvancedMessage advMsg) throws RequestErrorException {

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getSendMessageUrl();

        if (url==null) {
            logger.error("send advanced message url not set");
            throw new RequestErrorException("send advanced message url not set", ConnectionErrors.URL_ERROR);
        }

        String jsonStr = gson.toJson(advMsg);
        ContentResponse response = sendMessage(HttpMethod.POST, url, jsonStr);
        return gson.fromJson(response.getContentAsString(), MessagesResponse.class);
    }

    public DeliveryReport getReport() throws RequestErrorException {

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getReportsUrl();

        if (url==null) {
            logger.error("Delivery report url not set");
            throw new RequestErrorException("Delivery report url not set", ConnectionErrors.URL_ERROR);
        }

        ContentResponse response = sendMessage(HttpMethod.GET, url, null);
        return gson.fromJson(response.getContentAsString(), DeliveryReport.class);

    }



    public ImsiResponse getImsi(ImsiRequest imsiRequest) throws RequestErrorException {

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getImsiUrl();

        if (url==null) {
            logger.error("getImsi url not set");
            throw new RequestErrorException("Imsi url not set", ConnectionErrors.URL_ERROR);
        }

        String jsonStr = gson.toJson(imsiRequest);
        ContentResponse response = sendMessage(HttpMethod.POST, url, jsonStr);
        return gson.fromJson(response.getContentAsString(), ImsiResponse.class);
    }

    public ContentResponse sendMessage(HttpMethod method, String url, String content) throws RequestErrorException {

        logger.debug("Send message");
        ContentResponse response;
        try {


            Request request = httpClient.newRequest(url)
                    .method(method)
                    .agent("MessageGateServer HTTP client")
                    .version(HttpVersion.HTTP_1_1)
                    .timeout(timeOut, TimeUnit.SECONDS)
                    .header(HttpHeader.AUTHORIZATION, "Basic " + authorization)
                    .header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);

            if (content != null) {

                logger.debug("Message content: {}", content);
                request = request.content(new StringContentProvider(content));
            }

            response = request.send();



        } catch (InterruptedException e) {
            throw new RequestErrorException(e.getMessage(), ConnectionErrors.INTERRUPTED);
        } catch (ExecutionException e) {
            throw new RequestErrorException(e.getMessage(),getErrorType(e.getMessage()));
        } catch (TimeoutException e) {
            throw new RequestErrorException(e.getMessage(), ConnectionErrors.TIMEOUT);
        }

        logger.debug("Response status: {}, Type: {}", response.getStatus(), response.getMediaType());
        logger.debug("Response content: {}", new String (response.getContent()));

        return response;
    }

    private static ConnectionErrors getErrorType(String error){
        ConnectionErrors result = ConnectionErrors.UNKNOWN_ERROR;
        if (error.contains("HTTP protocol violation: Authentication challenge without")) result = ConnectionErrors.AUTH_ERROR;
        if (error.contains("HTTP/1.1 407 Proxy Authentication Required")) result = ConnectionErrors.PROXY_ERROR;
        if (error.contains("java.net.ConnectException")) result = ConnectionErrors.CONNECTION_ERROR;

        return result;
    }
}
