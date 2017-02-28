package com.bankir.mgs.jersey;

import com.bankir.mgs.Authorization;
import com.bankir.mgs.BasicAuth;
import com.bankir.mgs.Config;
import com.bankir.mgs.User;
import com.bankir.mgs.hibernate.dao.UserDAO;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class RestAuthorizationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthorizationFilter.class);

    @Context
    HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext)throws IOException {


        /*  Авторизация работает либо по заголовку Authorization  */


        /* Получаем экземпляр сессии */
        HttpSession httpSession = request.getSession();

        /* Текущий пользователь - либо авторизованный, либо аноним */
        User user = (User) httpSession.getAttribute("user");

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);


        /* Если заголовок присутствует, авторизуемся по нему */
        if (authorizationHeader!=null) {

            /* Если текущий заголовок не совпадает с заголовком, сохраненным в сессии, значит изменился пользователь,
            * переавторизовываем */

            if (!authorizationHeader.equals(httpSession.getAttribute(HttpHeaders.AUTHORIZATION))) {

                /* Если авторизация не удастся, то скидываем пользователя на анонима */
                user = Config.ANONYMOUS_USER;

                /* Распаковываем из заголовка логин и пароль*/
                String[] lap = BasicAuth.decode(authorizationHeader);
                /* Если у нас массив с 2-мя элементами (логин и пароль), то обрабатываем дальше */
                if (lap!=null&&lap.length == 2) {

                    try {
                        user = Authorization.Authorize(lap[0], lap[1]);

                        /* Для авторизованного пользователя сохраняем пользователя и заголовок Authorization в данные сессии */
                        if (!user.isAnonymous()) {
                            httpSession.setAttribute("user", user);
                            httpSession.setAttribute(HttpHeaders.AUTHORIZATION, authorizationHeader);
                        }

                    } catch (PasswordStorage.InvalidHashException e) {
                        logger.error("Error: "+e.getMessage(),e);
                    } catch (PasswordStorage.CannotPerformOperationException e) {
                        logger.error("Error: "+e.getMessage(),e);
                    }

                }
            } else {

                /* Проверяем, вдруг пользователя уже блокировали или удалили */
                StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
                UserDAO userDAO = new UserDAO(session);
                com.bankir.mgs.hibernate.model.User usr = userDAO.getByLogin(user.getLogin());
                if (usr==null||usr.isLocked()) user = Config.ANONYMOUS_USER;
                session.close();

            }
        }
        String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
        requestContext.setSecurityContext(new AppSecurityContext(user, scheme));

    }

    /*
    private static void authorize(ContainerRequestContext requestContext, String sessionId){
        Response response = JsonObject.getErrorResponse(Response.Status.UNAUTHORIZED, Config.MSG_UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE,"Basic realm=\"123\"");
        requestContext.abortWith(response);
    }
    */
}

