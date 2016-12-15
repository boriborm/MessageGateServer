package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.MessageType;
import org.hibernate.JDBCException;

public interface MessageTypeImpl {

    void add(MessageType row) throws JDBCException;
    void save(MessageType row) throws JDBCException;
    MessageType getById(String typeId) throws JDBCException;
    void delete(MessageType row) throws JDBCException;
}
