package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.UserMessageTypeImpl;
import com.bankir.mgs.hibernate.model.UserMessageType;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

import java.util.List;

public class UserMessageTypeDAO implements UserMessageTypeImpl {

    private StatelessSession session;
    public UserMessageTypeDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(UserMessageType row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(UserMessageType row) throws JDBCException {
        session.update(row);
    }

    @Override
    public UserMessageType get(String typeId, Long userId) throws JDBCException {
        return  (UserMessageType) session
            .createQuery("from UserMessageType where typeId = :typeId  and userId = :userId" )
            .setParameter("typeId", typeId)
            .setParameter("userId", userId)
            .uniqueResult();
    }

    @Override
    public void delete(UserMessageType row) throws JDBCException {
        session.delete(row);
    }

    public List<UserMessageType> getByUserId(Long userId) throws JDBCException {
        return  (List<UserMessageType>) session
                .createQuery("from UserMessageType where userId=:userId")
                .setParameter("userId", userId)
                .list();
    }

}
