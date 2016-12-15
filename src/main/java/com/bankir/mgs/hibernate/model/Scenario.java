package com.bankir.mgs.hibernate.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="SCENARIOS")
public class Scenario implements Serializable {

    @Id
    @SequenceGenerator(name="SEQ_SCENARIOID", sequenceName="SEQ_SCENARIOID",allocationSize=1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="SEQ_SCENARIOID")
    @Column(name="id")
    private Long id;

    @Column(name="scenarioKey")
    private String scenarioKey;

    @Column(name="scenarioName")
    private String scenarioName;

    @Column(name="infobipLogin")
    private String infobipLogin;

    @Type(type="yes_no")
    @Column(name="active")
    private boolean active;

    @Column(name="flow")
    private String flow;

    public Scenario(){}

    public Scenario(String scenarioKey, String scenarioName, String flow, boolean active, String infobipLogin) {
        this.scenarioKey = scenarioKey;
        this.scenarioName = scenarioName;
        this.infobipLogin = infobipLogin;
        this.active = active;
        this.flow = flow;
    }

    public String getScenarioKey() {
        return scenarioKey;
    }

    public void setScenarioKey(String scenarioKey) {
        this.scenarioKey = scenarioKey;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInfobipLogin() {
        return infobipLogin;
    }

    public void setInfobipLogin(String infobipLogin) {
        this.infobipLogin = infobipLogin;
    }

    public boolean isActive() {
        return active;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
