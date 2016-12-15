package com.bankir.mgs;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by bankir on 20.10.16.
 */
public class AppSessionListener  implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent event) {
        /* При создании сессии по умолчанию будет анонимный пользователь */
        event.getSession().setAttribute("user", Config.ANONYMOUS_USER);
        event.getSession().setMaxInactiveInterval(Config.getSettings().getSessionTimeout());
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        System.out.println("Session Destroyed");
    }
}
