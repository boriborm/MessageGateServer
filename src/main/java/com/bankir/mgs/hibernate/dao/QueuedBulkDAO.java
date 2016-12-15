package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.QueuedBulkImpl;
import com.bankir.mgs.hibernate.model.QueuedBulk;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class QueuedBulkDAO implements QueuedBulkImpl {

    private StatelessSession session;
    public QueuedBulkDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(QueuedBulk row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(QueuedBulk row) throws JDBCException {
        session.update(row);
    }

    @Override
    public QueuedBulk getById(Long id) throws JDBCException {
        return (QueuedBulk) session.get(QueuedBulk.class, id);
    }

    @Override
    public void delete(QueuedBulk row) throws JDBCException {
        session.delete(row);
    }
}
