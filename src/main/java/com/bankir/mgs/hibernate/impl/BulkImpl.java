package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.Bulk;
import org.hibernate.JDBCException;

public interface BulkImpl {

    void add(Bulk row) throws JDBCException;
    void save(Bulk row) throws JDBCException;
    Bulk getById(Long id) throws JDBCException;
    void delete(Bulk row) throws JDBCException;
}
