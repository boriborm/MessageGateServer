package com.bankir.mgs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by bankir on 20.10.16.
 */
public class AppSessionListener  implements HttpSessionListener {
    private static final Logger logger = LoggerFactory.getLogger(AppSessionListener.class);
    public void sessionCreated(HttpSessionEvent event) {
        /* При создании сессии по умолчанию будет анонимный пользователь */
        event.getSession().setAttribute("user", Config.ANONYMOUS_USER);
        event.getSession().setMaxInactiveInterval(Config.getSettings().getSessionTimeout());
        logger.debug("Session started. Id: {}", event.getSession().getId());
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        logger.debug("Session destroyed. Id: {}", event.getSession().getId());
    }
}
