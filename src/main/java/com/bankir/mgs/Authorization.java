package com.bankir.mgs;

import com.bankir.mgs.hibernate.dao.UserDAO;
import com.bankir.mgs.jersey.PasswordStorage;
import org.hibernate.StatelessSession;

public class Authorization {

    public static User Authorize(String login, String pass) throws PasswordStorage.InvalidHashException, PasswordStorage.CannotPerformOperationException {

        User user = Config.ANONYMOUS_USER;

        /* По БД определяем пользователя и его роли */
        StatelessSession session = Config.getHibernateSessionFactory().openStatelessSession();
        UserDAO userDAO = new UserDAO(session);
        com.bankir.mgs.hibernate.model.User usr = userDAO.getByLogin(login);

        //Найден и не заблокирован
        if (usr != null && !usr.isLocked()) {

            /* проверка пароля */
            if (PasswordStorage.verifyPassword(pass, usr.getHashInfo())) {
                user = new User(usr.getId(), usr.getUserName(), usr.getRoles());
            }

        }

        session.close();
        return user;
    }
}
