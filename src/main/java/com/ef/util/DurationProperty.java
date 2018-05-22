package com.ef.util;

public enum DurationProperty {
    HOURLY("hourly"),
    DAILY("daily");
	
    private String property;

    DurationProperty(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
