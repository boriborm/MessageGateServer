package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.BulkMessageImpl;
import com.bankir.mgs.hibernate.model.BulkMessage;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

public class BulkMessageDAO implements BulkMessageImpl {

    private StatelessSession session;
    public BulkMessageDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(BulkMessage row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public BulkMessage getById(Long messageId, Long bulkId) throws JDBCException {

        Query query = session.createQuery("FROM BulkMessage bm where messageId=:messageId")
                .setParameter("messageId",messageId);

        return (BulkMessage) query.uniqueResult();
    }

    @Override
    public void delete(BulkMessage row) throws JDBCException {
        session.delete(row);
    }

    public void delete(Long messageId, Long bulkId) throws JDBCException {
        BulkMessage qm = new BulkMessage(messageId, bulkId);
        session.delete(qm);
    }

}
