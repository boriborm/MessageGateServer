package com.bankir.mgs.hibernate.dao;


import com.bankir.mgs.hibernate.impl.ScenarioChannelImpl;
import com.bankir.mgs.hibernate.model.ScenarioChannel;
import org.hibernate.JDBCException;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.List;

public class ScenarioChannelDAO implements ScenarioChannelImpl {


    private StatelessSession session;
    public ScenarioChannelDAO(StatelessSession session){
        this.session=session;
    }

    @Override
    public void add(ScenarioChannel row) throws JDBCException {
        session.insert(row);
    }

    @Override
    public void save(ScenarioChannel row) throws JDBCException {
        session.update(row);
    }

    public List<ScenarioChannel> getScenarios(Long scenarioId) throws JDBCException {
        Query query = session.createQuery("from ScenarioChannel where scenarioId = :scenarioId order by priority asc")
                .setParameter("scenarioId", scenarioId);
        return (List<ScenarioChannel>) query.list();
    }

    @Override
    public ScenarioChannel getById(Long id) throws JDBCException {
        return (ScenarioChannel) session.get(ScenarioChannel.class, id);
    }

    @Override
    public void delete(ScenarioChannel row) throws JDBCException {
        session.delete(row);
    }

    public Long addScenarioChannel(Long scenarioId, String channel, int priority) throws JDBCException {
        ScenarioChannel newScenario = new ScenarioChannel(scenarioId, channel, priority);
        this.add(newScenario);
        return newScenario.getId();
    }
}

