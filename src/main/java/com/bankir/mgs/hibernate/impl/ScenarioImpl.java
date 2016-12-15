package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.Scenario;
import org.hibernate.JDBCException;

public interface ScenarioImpl {

    void add(Scenario row) throws JDBCException;
    void save(Scenario row) throws JDBCException;
    Scenario getById(Long id) throws JDBCException;
    void delete(Scenario row) throws JDBCException;
}
