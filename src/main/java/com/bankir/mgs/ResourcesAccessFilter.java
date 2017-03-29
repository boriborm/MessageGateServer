package com.bankir.mgs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class ResourcesAccessFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(ResourcesAccessFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        /* Фильрация при наличии заголовка Authorization для REST пути не производится */

        /* Идентифицируемся по заголовку Authorization */
        String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);



        /* Если задан заголовок Authorization и запрос к REST сервлетам, то ничего не фильтруем, пропускаем */
        if (authorizationHeader!=null&&Config.REST_PATH.equals(httpServletRequest.getServletPath())){
            chain.doFilter(request, response);
            return;
        }


        URI uri = null;
        try {
            uri = new URI(httpServletRequest.getRequestURI());

            HttpSession httpSession = httpServletRequest.getSession();
            User user = (User) httpSession.getAttribute("user");

            Settings settings = Config.getSettings();

            /* Если путь к административным URI, а пользователь не имеет роли ADMIN
            * то редиректим на страницу с информацией о недоступности ресурса */
            if (uri.getPath().startsWith(settings.getAdminPath()) && !user.userWithRole(User.ROLE_ADMIN)){
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendRedirect("/authorization.html?back=" + URLEncoder.encode(settings.getAdminPath(), "UTF-8"));
                return;
            }

            /* Если путь только для авторизованных пользователей, а пользователь анонимный
            * то редиректим на страницу с информацией о недоступности ресурса */
            if (uri.getPath().startsWith(settings.getOpersPath()) && user.isAnonymous()){
                logger.debug("session:"+ httpSession.getId()+ " uri path: "+uri.getPath()+" "+settings.getOpersPath() +" user is anonymous: "+user.isAnonymous());
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendRedirect("/authorization.html?back=" + URLEncoder.encode(settings.getOpersPath(), "UTF-8"));
                return;
            }

            chain.doFilter(request, response);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {

    }
}
