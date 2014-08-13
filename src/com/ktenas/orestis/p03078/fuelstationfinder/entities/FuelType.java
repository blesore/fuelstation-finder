package com.ktenas.orestis.p03078.fuelstationfinder.entities;

import java.util.Date;

public class FuelType {
	private String ôype;
	private float price;
	private Date lastUpdated;

	private FuelType(String ôype, float price, Date lastUpdated) {
		super();
		this.ôype = ôype;
		this.price = price;
		this.lastUpdated = lastUpdated;
	}

	public String getFuelType() {
		return ôype;
	}

	public void setFuelType(String fuelType) {
		this.ôype = fuelType;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
