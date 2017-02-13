package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.model.Imsi;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class ImsiDAO {

    private StatelessSession session;
    public ImsiDAO(StatelessSession session){
        this.session=session;
    }


    public void add(Imsi row) throws JDBCException {
        session.insert(row);
    }

    public void save(Imsi row) throws JDBCException {
        session.update(row);
    }

    public Imsi getById(String phoneNumber) throws JDBCException {
        return (Imsi) session.get(Imsi.class, phoneNumber);
    }

    public void delete(Imsi row) throws JDBCException {
        session.delete(row);
    }

}
