package com.ktenas.orestis.p03078.fuelstationfinder.entities;

import java.util.Date;

public class FuelType {
	private String �ype;
	private float price;
	private Date lastUpdated;

	private FuelType(String �ype, float price, Date lastUpdated) {
		super();
		this.�ype = �ype;
		this.price = price;
		this.lastUpdated = lastUpdated;
	}

	public String getFuelType() {
		return �ype;
	}

	public void setFuelType(String fuelType) {
		this.�ype = fuelType;
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
