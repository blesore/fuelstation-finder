package com.ktenas.orestis.p03078.fuelstationfinder.entities;

import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.ktenas.orestis.p03078.fuelstationfinder.enums.StationBrand;

public class FuelStation implements Parcelable {
    private long stationCode;
    private String address;
    private String ownerName;
    private StationBrand brand;
    private double[] location;
    private List<Fuel> availableFuel;
    private Date lastUpdated;
    private boolean isCheapestInRange = false;

    public FuelStation(){};
    
    public FuelStation(long stationCode, String address, String ownerName, StationBrand brand, double[] location, List<Fuel> availableFuel, Date lastUpdated,
            boolean isCheapestInRange) {
        super();
        this.stationCode = stationCode;
        this.address = address;
        this.ownerName = ownerName;
        this.brand = brand;
        this.location = location;
        this.availableFuel = availableFuel;
        this.lastUpdated = lastUpdated;
        this.isCheapestInRange = isCheapestInRange;
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

    public LatLng getLocation() {
        return new LatLng(location[0], location[1]);
    }

    public void setLocation(double[] location) {
        this.location = location;
    }

    public List<Fuel> getAvailableFuel() {
        return availableFuel;
    }

    public void setAvailableFuel(List<Fuel> availableFuel) {
        this.availableFuel = availableFuel;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
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

    public boolean isCheapestInRange() {
        return isCheapestInRange;
    }

    public void setCheapestInRange(boolean isCheapestInRange) {
        this.isCheapestInRange = isCheapestInRange;
    }
}
