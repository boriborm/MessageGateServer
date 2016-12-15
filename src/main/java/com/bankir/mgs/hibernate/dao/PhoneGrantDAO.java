package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.impl.PhoneGrantImpl;
import com.bankir.mgs.hibernate.model.PhoneGrant;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class PhoneGrantDAO implements PhoneGrantImpl {

    private StatelessSession session;
    public PhoneGrantDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(PhoneGrant row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(PhoneGrant row) throws JDBCException {
        session.update(row);
    }

    @Override
    public PhoneGrant getById(String phoneNumber) throws JDBCException {
        return (PhoneGrant) session.get(PhoneGrant.class, phoneNumber);
    }

    @Override
    public void delete(PhoneGrant row) throws JDBCException {
        session.delete(row);
    }

}
