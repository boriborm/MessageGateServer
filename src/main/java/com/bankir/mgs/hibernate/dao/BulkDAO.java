package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.BulkImpl;
import com.bankir.mgs.hibernate.model.Bulk;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class BulkDAO implements BulkImpl {

    private StatelessSession session;
    public BulkDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(Bulk row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(Bulk row) throws JDBCException {
        session.update(row);
    }

    @Override
    public Bulk getById(Long id) throws JDBCException {
        return (Bulk) session.get(Bulk.class, id);
    }

    @Override
    public void delete(Bulk row) throws JDBCException {
        session.delete(row);
    }
}
