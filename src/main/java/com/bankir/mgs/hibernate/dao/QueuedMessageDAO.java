package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.QueuedMessageImpl;
import com.bankir.mgs.hibernate.model.QueuedMessage;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class QueuedMessageDAO implements QueuedMessageImpl {

    private StatelessSession session;
    public QueuedMessageDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(QueuedMessage row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public QueuedMessage getById(Long id) throws JDBCException {
        return (QueuedMessage) session.get(QueuedMessage.class, id);
    }

    @Override
    public void delete(QueuedMessage row) throws JDBCException {
        session.delete(row);
    }


    public void delete(Long messageId) throws JDBCException {
        QueuedMessage qm = new QueuedMessage(messageId);
        session.delete(qm);
    }

}
