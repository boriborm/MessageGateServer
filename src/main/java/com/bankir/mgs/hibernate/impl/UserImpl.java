package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.User;
import org.hibernate.JDBCException;

public interface UserImpl {

    void add(User row) throws JDBCException;
    void save(User row) throws JDBCException;
    User getById(Long id) throws JDBCException;
    User getByLogin(String login) throws JDBCException;
    void delete(User row) throws JDBCException;
}
