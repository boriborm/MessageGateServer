package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.MessageTypeImpl;
import com.bankir.mgs.hibernate.model.MessageType;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class MessageTypeDAO implements MessageTypeImpl {

    private StatelessSession session;
    public MessageTypeDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(MessageType row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(MessageType row) throws JDBCException {
        session.update(row);
    }

    @Override
    public MessageType getById(String typeId) throws JDBCException {
        return (MessageType) session.get(MessageType.class, typeId);
    }

    @Override
    public void delete(MessageType row) throws JDBCException {
        session.delete(row);
    }

}
