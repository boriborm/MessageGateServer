package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.QueuedBulkMessage;
import org.hibernate.JDBCException;

public interface QueuedBulkMessageImpl {

    void add(QueuedBulkMessage row) throws JDBCException;
    QueuedBulkMessage getById(Long id) throws JDBCException;
    void delete(QueuedBulkMessage row) throws JDBCException;
}
