package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.UserImpl;
import com.bankir.mgs.hibernate.model.User;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

public class UserDAO implements UserImpl {

    private StatelessSession session;
    public UserDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(User row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(User row) throws JDBCException {
        session.update(row);
    }

    @Override
    public User getById(Long id) throws JDBCException {
        return (User) session.get(User.class, id);
    }

    public User getByLogin(String login){
        Query query = session.createQuery("from User where login=:login").setParameter("login", login);
        return (User) query.uniqueResult();
    }

    @Override
    public void delete(User row) throws JDBCException {
        session.delete(row);
    }

}
