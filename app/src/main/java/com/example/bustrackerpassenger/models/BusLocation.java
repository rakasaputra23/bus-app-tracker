package com.example.bustrackerpassenger.models;

public class BusLocation {
    private double latitude;
    private double longitude;
    private float speed;
    private String lastUpdate;

    public BusLocation() {
        // Required empty constructor for Firebase
    }

    public BusLocation(double latitude, double longitude, float speed, String lastUpdate) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.lastUpdate = lastUpdate;
    }

    // Getters and Setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
}