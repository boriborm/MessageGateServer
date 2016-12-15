package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.ReportImpl;
import com.bankir.mgs.hibernate.model.Report;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class ReportDAO implements ReportImpl {

    private StatelessSession session;
    public ReportDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(Report row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(Report row) throws JDBCException {
        session.update(row);
    }

    @Override
    public Report getById(Long id) throws JDBCException {
        return (Report) session.get(Report.class, id);
    }

    @Override
    public void delete(Report row) throws JDBCException {
        session.delete(row);
    }

}
