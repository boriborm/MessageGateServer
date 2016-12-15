package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.QueuedMessage;
import org.hibernate.JDBCException;

public interface QueuedMessageImpl {

    void add(QueuedMessage row) throws JDBCException;
    QueuedMessage getById(Long id) throws JDBCException;
    void delete(QueuedMessage row) throws JDBCException;
}
