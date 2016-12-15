package com.bankir.mgs.hibernate.dao;


import com.bankir.mgs.hibernate.impl.ScenarioImpl;
import com.bankir.mgs.hibernate.model.Scenario;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

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

        return (Scenario) query.uniqueResult();
    }

    @Override
    public Scenario getById(Long id) throws JDBCException {
        return (Scenario) session.get(Scenario.class, id);
    }

    @Override
    public void delete(Scenario row) throws JDBCException {
        session.delete(row);
    }

    public Long addScenario(String scenarioKey, String scenarioName, String flow, boolean isActive, String login) throws JDBCException {
        Scenario newScenario = new Scenario(scenarioKey, scenarioName, flow, isActive, login);
        this.add(newScenario);
        return newScenario.getId();
    }
}

