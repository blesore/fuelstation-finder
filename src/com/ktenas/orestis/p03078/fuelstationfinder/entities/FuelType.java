package com.ktenas.orestis.p03078.fuelstationfinder.entities;

public enum FuelType {
	UNLEADED_95("Uleaded 95"),
	UNLEADED_100("Uleaded 100"),
	DIESEL("Diesel"),
	AUTOGAS("Autogas");
	
	private String title;

	private FuelType(String title) {
		this.setTitle(title);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
