package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.PhoneGrant;
import org.hibernate.JDBCException;

public interface PhoneGrantImpl {

    void add(PhoneGrant row) throws JDBCException;
    void save(PhoneGrant row) throws JDBCException;
    PhoneGrant getById(String phoneNumber) throws JDBCException;
    void delete(PhoneGrant row) throws JDBCException;
}
