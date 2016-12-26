package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.UserMessageType;
import org.hibernate.JDBCException;

public interface UserMessageTypeImpl {

    void add(UserMessageType row) throws JDBCException;
    void save(UserMessageType row) throws JDBCException;
    UserMessageType get(String typeId, Long userId) throws JDBCException;
    void delete(UserMessageType row) throws JDBCException;

}
