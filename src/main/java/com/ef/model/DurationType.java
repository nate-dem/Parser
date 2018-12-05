package com.ef.model;

public enum DurationType {
	HOURLY("hourly"), DAILY("daily");

	private String value;

	DurationType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static DurationType fromValue(String v) {
		for (DurationType rt : DurationType.values()) {
			if (rt.value.equalsIgnoreCase(v)) {
				return rt;
			}
		}
		throw new IllegalArgumentException("Invalid Duration: " + v);
	}

}
