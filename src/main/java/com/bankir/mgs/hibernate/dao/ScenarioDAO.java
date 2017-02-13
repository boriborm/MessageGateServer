package com.bankir.mgs.hibernate.dao;


import com.bankir.mgs.hibernate.impl.ScenarioImpl;
import com.bankir.mgs.hibernate.model.Scenario;
import com.bankir.mgs.jersey.model.ScenarioObject;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.List;

public class ScenarioDAO implements ScenarioImpl {


    private StatelessSession session;
    public ScenarioDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(Scenario row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(Scenario row) throws JDBCException {
        session.update(row);
    }

    public Scenario getByKey(String scenarioKey, String infobipLogin) throws JDBCException {
        Query query = session.createQuery("from Scenario where scenarioKey = :key and infobipLogin = :login")
                .setParameter("key", scenarioKey)
                .setParameter("login", infobipLogin);
        Scenario scenario = (Scenario) query.uniqueResult();
        scenario.parseFlow();
        return scenario;
    }

    public Scenario getByChannels(String channels, String infobipLogin) throws JDBCException {
        Query query = session.createQuery("from Scenario where channels = :channels and infobipLogin = :login")
                .setParameter("channels", channels)
                .setParameter("login", infobipLogin);
        Scenario scenario = (Scenario) query.getSingleResult();
        scenario.parseFlow();
        return scenario;
    }

    @Override
    public Scenario getById(Long id) throws JDBCException {
        Scenario scenario = (Scenario) session.get(Scenario.class, id);
        scenario.parseFlow();
        return scenario;
    }

    @Override
    public void delete(Scenario row) throws JDBCException {
        session.delete(row);
    }

    public Long addScenario(String scenarioKey, String scenarioName, List<ScenarioObject.Flow> flows, boolean isActive, String login) throws JDBCException {
        Scenario newScenario = new Scenario(scenarioKey, scenarioName, flows, isActive, login);
        this.add(newScenario);
        return newScenario.getId();
    }
}

