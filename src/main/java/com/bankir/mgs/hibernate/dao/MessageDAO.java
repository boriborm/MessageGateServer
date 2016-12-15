package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.MessageImpl;
import com.bankir.mgs.hibernate.model.Message;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

public class MessageDAO implements MessageImpl {

    private StatelessSession session;
    public MessageDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(Message row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(Message row) throws JDBCException {
        session.update(row);
    }

    @Override
    public Message getById(Long id) throws JDBCException {
        return (Message) session.get(Message.class, id);
    }

    @Override
    public void delete(Message row) throws JDBCException {
        session.delete(row);
    }

    public Message getByExternalId(String externalId) throws JDBCException {
        Query query = session.createQuery("from Message where externalId = :externalId ");
        query.setParameter("externalId", externalId);
        return  (Message) query.uniqueResult();
    }
}
