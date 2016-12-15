package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.QueuedBulk;
import org.hibernate.JDBCException;

public interface QueuedBulkImpl {

    void add(QueuedBulk row) throws JDBCException;
    void save(QueuedBulk row) throws JDBCException;
    QueuedBulk getById(Long id) throws JDBCException;
    void delete(QueuedBulk row) throws JDBCException;
}
