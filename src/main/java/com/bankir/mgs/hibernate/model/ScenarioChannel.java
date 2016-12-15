package com.bankir.mgs.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="SCENARIOCHANNELS")
public class ScenarioChannel implements Serializable {

    @Id
    @SequenceGenerator(name="SEQ_SCENARIOCHANNELID", sequenceName="SEQ_SCENARIOCHANNELID",allocationSize=1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="SEQ_SCENARIOCHANNELID")
    @Column(name="id")
    private Long id;

    @Column(name="scenarioId")
    private Long scenarioKey;

    @Column(name="channel")
    private String channel;

    @Column(name="priority")
    private int priority;

    public ScenarioChannel(){}

    public ScenarioChannel(Long scenarioKey, String channel, int priority) {
        this.scenarioKey = scenarioKey;
        this.channel = channel;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScenarioKey() {
        return scenarioKey;
    }

    public void setScenarioKey(Long scenarioKey) {
        this.scenarioKey = scenarioKey;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
