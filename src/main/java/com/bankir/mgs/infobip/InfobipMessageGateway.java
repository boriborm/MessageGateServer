package com.bankir.mgs.infobip;

import com.bankir.mgs.Config;
import com.bankir.mgs.Settings;
import com.bankir.mgs.infobip.model.DeliveryReport;
import com.bankir.mgs.infobip.model.MessagesResponse;
import com.bankir.mgs.infobip.model.OmniAdvancedMessage;
import com.bankir.mgs.infobip.model.RequestError;
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
    private String url;
    private HttpClient httpClient;
    private String authorization;
    private static final int  timeOut = 15;
    private static Gson gson;

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

    public MessagesResponse sendAdvancedMessage(OmniAdvancedMessage advMsg) {

        MessagesResponse result;
        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getSendMessageUrl();
        String jsonStr = gson.toJson(advMsg);
        ContentResponse response = null;
        try {
            response = sendMessage(HttpMethod.POST, url, jsonStr);
            result = gson.fromJson(response.getContentAsString(), MessagesResponse.class);
        } catch (InterruptedException e) {
            logger.error("sendAdvancedMessage interrupt exception");
            result = new MessagesResponse();
            RequestError requestError = new RequestError(e.getMessage(), "INTERRUPT_ERROR");
            result.setRequestError(requestError);
        } catch (ExecutionException e) {
            logger.error("sendAdvancedMessage execution exception");
            result = new MessagesResponse();
            RequestError requestError = new RequestError(e.getMessage(), getErrorType(e.getMessage()));
            result.setRequestError(requestError);
        } catch (TimeoutException e) {
            logger.error("sendAdvancedMessage timeout exception");
            result = new MessagesResponse();
            RequestError requestError = new RequestError(e.getMessage(), "TIMEOUT_ERROR");
            result.setRequestError(requestError);
        }

        // Парсим ответ в JsonObject и отдаём
        return result;
    }

    public ContentResponse sendMessage(HttpMethod method, String url, String content) throws InterruptedException, ExecutionException, TimeoutException {

            logger.debug("Send message");
            Request request = httpClient.newRequest(url)
                    .method(method)
                    .agent("MessageGateServer HTTP client")
                    .version(HttpVersion.HTTP_1_1)
                    .timeout(timeOut, TimeUnit.SECONDS)
                    .header(HttpHeader.AUTHORIZATION, "Basic "+authorization)
                    .header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            if (content!=null){

                logger.debug("Message content: {}", content);
                request = request.content(new StringContentProvider(content));
            }

            ContentResponse response = request.send();
            logger.debug("Response status: {}, Type: {}", response.getStatus(), response.getMediaType());
            logger.debug("Response content: {}", new String (response.getContent()));
            return response;
    }


    public DeliveryReport getReport(){

        DeliveryReport result;

        Settings settings = Config.getSettings();
        String url = settings.getInfobip().getReportsUrl();

        try {
            ContentResponse response = sendMessage(HttpMethod.GET, url, null);
            result = gson.fromJson(response.getContentAsString(), DeliveryReport.class);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error: "+ e.getMessage(), e);
            result = null;
        }

        return result;

    }

    private static String getErrorType(String error){
        String result = "ERROR";
        if (error.startsWith("HTTP protocol violation: Authentication challenge without")) result = "AUTH_ERROR";
        if (error.startsWith("HTTP/1.1 407 Proxy Authentication Required")) result = "PROXY_ERROR";
        return result;
    }
}
