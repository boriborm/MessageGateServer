package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.QueuedBulkMessageImpl;
import com.bankir.mgs.hibernate.model.QueuedBulkMessage;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class QueuedBulkMessageDAO implements QueuedBulkMessageImpl {

    private StatelessSession session;
    public QueuedBulkMessageDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(QueuedBulkMessage row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public QueuedBulkMessage getById(Long id) throws JDBCException {
        return (QueuedBulkMessage) session.get(QueuedBulkMessage.class, id);
    }

    @Override
    public void delete(QueuedBulkMessage row) throws JDBCException {
        session.delete(row);
    }


    public void delete(Long messageId, Long bulkId) throws JDBCException {
        QueuedBulkMessage qm = new QueuedBulkMessage(messageId, bulkId);
        session.delete(qm);
    }

}
