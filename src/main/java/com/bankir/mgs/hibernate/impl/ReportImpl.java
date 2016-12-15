package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.Report;
import org.hibernate.JDBCException;

public interface ReportImpl {

    void add(Report row) throws JDBCException;
    void save(Report row) throws JDBCException;
    Report getById(Long id) throws JDBCException;
    void delete(Report row) throws JDBCException;
}
