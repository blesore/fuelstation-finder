package com.ktenas.orestis.p03078.fuelstationfinder.entities;

import java.util.List;

public class FuelStation {
	private long stationCode;
	private String address;
	private String ownerName;
	private String brand;
	private double longitude;
	private double latitude;
	private List<FuelType> fuelTypes;

	private FuelStation(long stationCode, String address, String ownerName,
			String brand, double longitude, double latitude,
			List<FuelType> fuelTypes) {
		super();
		this.stationCode = stationCode;
		this.address = address;
		this.ownerName = ownerName;
		this.brand = brand;
		this.longitude = longitude;
		this.latitude = latitude;
		this.fuelTypes = fuelTypes;
	}

	public long getStationCode() {
		return stationCode;
	}

	public void setStationCode(long stationCode) {
		this.stationCode = stationCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public List<FuelType> getFuelTypes() {
		return fuelTypes;
	}

	public void setFuelTypes(List<FuelType> fuelTypes) {
		this.fuelTypes = fuelTypes;
	}
}
