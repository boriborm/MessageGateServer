package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.Message;
import org.hibernate.JDBCException;

public interface MessageImpl {

    void add(Message row) throws JDBCException;
    void save(Message row) throws JDBCException;
    Message getById(Long id) throws JDBCException;
    void delete(Message row) throws JDBCException;
}
