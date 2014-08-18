package com.ktenas.orestis.p03078.fuelstationfinder.entities;

import java.util.Date;

public class Fuel {
	private FuelType type;
	private float price;
	private Date lastUpdated;

	private Fuel(FuelType type, float price, Date lastUpdated) {
		super();
		this.type = type;
		this.price = price;
		this.lastUpdated = lastUpdated;
	}

	public FuelType getFuelType() {
		return type;
	}

	public void setFuelType(FuelType fuelType) {
		this.type = fuelType;
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
