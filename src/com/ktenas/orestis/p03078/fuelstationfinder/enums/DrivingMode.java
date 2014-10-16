package com.ktenas.orestis.p03078.fuelstationfinder.enums;

public enum DrivingMode {
    CITY(16, 500, 30),
    HYBRID(15, 1000, 20),
    TRIP(14, 5000, 10);

    private int zoom;
    private int distanceThreshold;
    private int numOfPoints;

    private DrivingMode(int zoom, int distanceThreshold, int numOfPoints) {
        this.setZoom(zoom);
        this.setDistanceThreshold(distanceThreshold);
        this.setNumOfPoints(numOfPoints);
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public int getDistanceThreshold() {
        return distanceThreshold;
    }

    public void setDistanceThreshold(int distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }

    public int getNumOfPoints() {
        return numOfPoints;
    }

    public void setNumOfPoints(int numOfPoints) {
        this.numOfPoints = numOfPoints;
    }
}
