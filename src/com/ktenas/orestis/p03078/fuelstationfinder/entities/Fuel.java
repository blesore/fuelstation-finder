package com.ktenas.orestis.p03078.fuelstationfinder.entities;


public class Fuel {
	private FuelType type;
	private float price;

	public Fuel(FuelType type, float price) {
		super();
		this.type = type;
		this.price = price;
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
}
