package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.BulkMessage;
import org.hibernate.JDBCException;

public interface BulkMessageImpl {

    void add(BulkMessage row) throws JDBCException;
    BulkMessage getById(Long messageId, Long bulkId) throws JDBCException;
    void delete(BulkMessage row) throws JDBCException;
}
