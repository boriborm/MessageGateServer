package com.bankir.mgs.infobip;

import com.bankir.mgs.Config;
import com.bankir.mgs.Settings;
import com.bankir.mgs.infobip.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
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
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
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


    public enum ConnectionErrors  {UNKNOWN_ERROR, URL_ERROR, PROXY_ERROR, AUTH_ERROR, INTERRUPTED, CONNECTION_ERROR, TIMEOUT }

    public static class RequestErrorException extends Exception {
        private ConnectionErrors type;
        private String message;
        private Exception e;
        RequestErrorException(String message, ConnectionErrors type, Exception e) {
            super(message);
            this.e = e;
            this.message = message;
            this.type = type;
        }

        public ConnectionErrors getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return (message==null?e.getMessage():message);
        }

        public Exception getOriginalException() {
            return e;
        }
    }

    public InfobipMessageGateway() throws RequestErrorException {
        Settings settings = Config.getSettings();

        authorization = Config.INFOBIP_AUTHORIZATION;
        SslContextFactory sslContextFactory = new SslContextFactory();
        httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);

        if (settings.isUseProxy()) {
            // Proxy credentials.
            AuthenticationStore auth = httpClient.getAuthenticationStore();

            Settings.Proxy proxy = settings.getProxy();

            try {
                auth.addAuthentication(
                        new BasicAuthentication(
                                new URI(proxy.getUrl()),
                                proxy.getRealm(),
                                proxy.getLogin(),
                                proxy.getPassword()
                        )
                );
            } catch (URISyntaxException e) {
                throw new RequestErrorException(e.getMessage(), ConnectionErrors.URL_ERROR, e);
            }
            ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
            proxyConfig.getProxies().add(Config.getProxy());
        }

        try {
            httpClient.start();
        } catch (Exception e) {
            throw new RequestErrorException(e.getMessage(), ConnectionErrors.CONNECTION_ERROR, e);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();

        logger.debug("Start Infobip gateway connection. Thread id: {}", Thread.currentThread().getId());
    }

    public void stop(){
        try {
            httpClient.stop();
            logger.debug("Stop Infobip gateway connection. Thread id: {}", Thread.currentThread().getId());
        } catch (Exception e) {
            logger.error("Error: "+e.getMessage(), e);
        }
    }

    public MessagesResponse sendAdvancedMessage(InfobipObjects.OmniAdvancedMessage advMsg) throws RequestErrorException {

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getSendMessageUrl();

        if (url==null) {
            logger.error("send advanced message url not set");
            throw new RequestErrorException("Advanced message url not set", ConnectionErrors.URL_ERROR, null);
        }

        String jsonStr = gson.toJson(advMsg);
        JsonObject response = sendMessage(HttpMethod.POST, url, jsonStr);
        return gson.fromJson(response, MessagesResponse.class);
    }

    public MessagesResponse sendSimpleMessage(InfobipObjects.OmniSimpleMessage msg) throws RequestErrorException {

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getSendSimpleMessageUrl();

        if (url==null) {
            logger.error("send simple message url not set");
            throw new RequestErrorException("Simple message url not set", ConnectionErrors.URL_ERROR, null);
        }

        String jsonStr = gson.toJson(msg);
        JsonObject response = sendMessage(HttpMethod.POST, url, jsonStr);
        return gson.fromJson(response, MessagesResponse.class);
    }

    public DeliveryReport getReport() throws RequestErrorException {

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getReportsUrl();

        if (url==null) {
            logger.error("Delivery report url not set");
            throw new RequestErrorException("Delivery report url not set", ConnectionErrors.URL_ERROR, null);
        }

        JsonObject response = sendMessage(HttpMethod.GET, url, null);
        return gson.fromJson(response, DeliveryReport.class);

    }



    public ImsiResponse getImsi(ImsiRequest imsiRequest) throws RequestErrorException {

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getImsiUrl();

        if (url==null) {
            logger.error("getImsi url not set");
            throw new RequestErrorException("Imsi url not set", ConnectionErrors.URL_ERROR, null);
        }

        String jsonStr = gson.toJson(imsiRequest);
        JsonObject response = sendMessage(HttpMethod.POST, url, jsonStr);
        return gson.fromJson(response, ImsiResponse.class);
    }

    public JsonObject sendMessage(HttpMethod method, String url, String content) throws RequestErrorException {

        logger.debug("Send message");

        JsonObject result = null;
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

            ContentResponse response = request.send();

            logger.debug("Response status: {}, Type: {}", response.getStatus(), response.getMediaType());
            logger.debug("Response content: {}", new String (response.getContent()));


            result = new JsonParser().parse(response.getContentAsString()).getAsJsonObject();

            result.addProperty("httpStatus", response.getStatus());

            if (result.has("requestError")){
                InfobipObjects.RequestError requestError = gson.fromJson(result.getAsJsonObject("requestError"), InfobipObjects.RequestError.class);
                String errorId = requestError.getServiceException().getMessageId();

                ConnectionErrors errType = ConnectionErrors.UNKNOWN_ERROR;

                if ("GENERAL_ERROR".equalsIgnoreCase(errorId)) errType = ConnectionErrors.CONNECTION_ERROR;
                if ("COMMUNICATION_ERROR".equalsIgnoreCase(errorId)) errType = ConnectionErrors.CONNECTION_ERROR;
                if ("ERROR_PROCESSING".equalsIgnoreCase(errorId)) errType = ConnectionErrors.CONNECTION_ERROR;
                if ("SEND_ERROR".equalsIgnoreCase(errorId)) errType = ConnectionErrors.CONNECTION_ERROR;

                if ("INVALID_USER_OR_PASS".equalsIgnoreCase(errorId)) errType = ConnectionErrors.AUTH_ERROR;
                if ("MISSING_USERNAME".equalsIgnoreCase(errorId)) errType = ConnectionErrors.AUTH_ERROR;
                if ("MISSING_PASSWORD".equalsIgnoreCase(errorId)) errType = ConnectionErrors.AUTH_ERROR;
                if ("NOT_ENOUGH_CREDITS".equalsIgnoreCase(errorId)) errType = ConnectionErrors.AUTH_ERROR;



                throw new RequestErrorException(requestError.getServiceException().getText(), errType, null);
            }


        } catch (InterruptedException e) {
            throw new RequestErrorException(e.getMessage(), ConnectionErrors.INTERRUPTED, e);
        } catch (ExecutionException e) {
            throw new RequestErrorException(e.getMessage(),getErrorType(e, e.getMessage()), e);
        } catch (TimeoutException e) {
            throw new RequestErrorException(e.getMessage(), ConnectionErrors.TIMEOUT, e);
        }


        return result;
    }

    private static ConnectionErrors getErrorType(Throwable t, String error){
        ConnectionErrors result = ConnectionErrors.UNKNOWN_ERROR;

        Throwable cause = getThrowableCaused(t, HttpResponseException.class);
        if (cause!=null){
            result = ConnectionErrors.CONNECTION_ERROR;
        }

        cause = getThrowableCaused(t, ConnectException.class);
        if (cause!=null){
            result = ConnectionErrors.CONNECTION_ERROR;
        }

        if (error.contains("HTTP protocol violation: Authentication challenge without")) result = ConnectionErrors.AUTH_ERROR;
        if (error.contains("HTTP/1.1 407 Proxy Authentication Required")) result = ConnectionErrors.PROXY_ERROR;
        //if (error.contains("java.net.ConnectException")) result = ConnectionErrors.CONNECTION_ERROR;


        return result;
    }

    private static Throwable getThrowableCaused(Throwable e, Class clazz) {
        Throwable cause = null;
        Throwable result = e;

        while(null != (cause = result.getCause()) && (result != cause))  {
            result = cause;
            if (result.getClass().isAssignableFrom(clazz)){
                break;
            }
        }
        return result;
    }
}
