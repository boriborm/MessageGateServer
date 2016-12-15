package com.bankir.mgs.jersey.model;

import java.util.List;

public class ScenarioObject {

    private Long id;
    private String name;
    private String key;
    private boolean active;
    private boolean isDefault;
    private List<ScenarioObject.Flow> flow;

    public static class Flow {
        private String channel;
        private String from;

        public Flow(String channel, String from) {
            this.channel = channel;
            this.from = from;
        }

        public String getChannel() {
            return channel;
        }

        public String getFrom() {
            return from;
        }
    }

    public ScenarioObject(Long id, String name, String key, boolean active, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.active = active;
        this.isDefault = isDefault;
    }

    public void setFlow(List<Flow> flow) {
        this.flow = flow;
    }

    public List<Flow> getFlow() {
        return flow;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
