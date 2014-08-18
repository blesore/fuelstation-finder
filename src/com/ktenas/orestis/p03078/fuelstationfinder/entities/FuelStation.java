package com.ktenas.orestis.p03078.fuelstationfinder.entities;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class FuelStation implements Parcelable {
	private long stationCode;
	private String address;
	private String ownerName;
	private StationBrand brand;
	private LatLng position;
	private List<FuelType> fuelTypes;

	public FuelStation(long stationCode, String address, String ownerName,
			StationBrand brand, LatLng position,
			List<FuelType> fuelTypes) {
		super();
		this.stationCode = stationCode;
		this.address = address;
		this.ownerName = ownerName;
		this.brand = brand;
		this.position = position;
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

	public StationBrand getBrand() {
		return brand;
	}

	public void setBrand(StationBrand brand) {
		this.brand = brand;
	}

	public LatLng getPosition() {
		return position;
	}

	public void setLongitude(LatLng position) {
		this.position = position;
	}

	public List<FuelType> getFuelTypes() {
		return fuelTypes;
	}

	public void setFuelTypes(List<FuelType> fuelTypes) {
		this.fuelTypes = fuelTypes;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}
