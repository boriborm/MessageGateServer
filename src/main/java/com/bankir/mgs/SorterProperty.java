package com.bankir.mgs;

public class SorterProperty {
    String property;
    String direction = "ASC";

    public SorterProperty(String property, String direction) {
        this.property = property;
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }
    public String getDirection() {
        return direction;
    }
}
