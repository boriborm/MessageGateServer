package com.bankir.mgs.hibernate.dao;

import com.bankir.mgs.hibernate.model.NewImsi;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;

public class NewImsiDAO {

    private StatelessSession session;
    public NewImsiDAO(StatelessSession session){
        this.session=session;
    }


    public void add(NewImsi row) throws JDBCException {
        session.insert(row);
    }

    public void save(NewImsi row) throws JDBCException {
        session.update(row);
    }

    public NewImsi getById(String phoneNumber) throws JDBCException {
        return (NewImsi) session.get(NewImsi.class, phoneNumber);
    }

    public void delete(NewImsi row) throws JDBCException {
        session.delete(row);
    }

}
