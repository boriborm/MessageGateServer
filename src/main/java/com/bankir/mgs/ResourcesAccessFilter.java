package com.bankir.mgs;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.URLEncoder;

public class ResourcesAccessFilter implements Filter {



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

        String uri =  httpServletRequest.getRequestURI();

        //System.out.println(uri);

        HttpSession httpSession = httpServletRequest.getSession();
        User user = (User) httpSession.getAttribute("user");

        Settings settings = Config.getSettings();


        /* Если путь к административным URI, а пользователь не имеет роли ADMIN
        * то редиректим на страницу с информацией о недоступности ресурса */
        if (uri.matches(settings.getAdminUriMatch()) && !user.userWithRole(User.ROLE_ADMIN)){
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect("/authorization.html?back=" + URLEncoder.encode(uri, "UTF-8"));
            //System.out.println("redirect to authorization 1. uri "+uri);
            return;
        }

        /* Если путь только для авторизованных пользователей, а пользователь анонимный
        * то редиректим на страницу с информацией о недоступности ресурса */
        if (uri.matches(settings.getAuthorizedUriMatch()) && user.isAnonymous()){
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect("/authorization.html?back=" + URLEncoder.encode(uri, "UTF-8"));
            //System.out.println("redirect to authorization 2. uri "+uri);
            return;
        }

        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {

    }
}
