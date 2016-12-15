package com.bankir.mgs.hibernate.impl;

import com.bankir.mgs.hibernate.model.ScenarioChannel;
import org.hibernate.JDBCException;

public interface ScenarioChannelImpl {

    void add(ScenarioChannel row) throws JDBCException;
    void save(ScenarioChannel row) throws JDBCException;
    ScenarioChannel getById(Long id) throws JDBCException;
    void delete(ScenarioChannel row) throws JDBCException;
}
