package com.example.bustrackerpassenger.models;

import java.util.ArrayList;
import java.util.List;

public class Bus {
    private String busId;
    private String plateNumber;
    private String busClass;
    private String route;
    private int capacity;
    private int currentPassengers;
    private String driver;
    private String status;
    private BusLocation location;
    private List<TrackPoint> track;
    private String encodedRoute; // ===== TAMBAHAN BARU untuk Planned Route =====

    // Empty constructor - WAJIB untuk Firebase
    public Bus() {
        this.track = new ArrayList<>();
    }

    // Constructor dengan parameter
    public Bus(String busId, String plateNumber, String busClass, String route,
               int capacity, int currentPassengers, String driver,
               String status, BusLocation location) {
        this.busId = busId;
        this.plateNumber = plateNumber;
        this.busClass = busClass;
        this.route = route;
        this.capacity = capacity;
        this.currentPassengers = currentPassengers;
        this.driver = driver;
        this.status = status;
        this.location = location;
        this.track = new ArrayList<>();
    }

    // Getters and Setters
    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getBusClass() {
        return busClass;
    }

    public void setBusClass(String busClass) {
        this.busClass = busClass;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentPassengers() {
        return currentPassengers;
    }

    public void setCurrentPassengers(int currentPassengers) {
        this.currentPassengers = currentPassengers;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BusLocation getLocation() {
        return location;
    }

    public void setLocation(BusLocation location) {
        this.location = location;
    }

    public List<TrackPoint> getTrack() {
        return track;
    }

    public void setTrack(List<TrackPoint> track) {
        this.track = track;
    }

    // ===== GETTER DAN SETTER BARU untuk Encoded Route =====
    public String getEncodedRoute() {
        return encodedRoute;
    }

    public void setEncodedRoute(String encodedRoute) {
        this.encodedRoute = encodedRoute;
    }

    // Helper methods
    public int getAvailableSeats() {
        return capacity - currentPassengers;
    }

    public String getOccupancyText() {
        return currentPassengers + "/" + capacity;
    }

    // Inner class untuk Track Point
    public static class TrackPoint {
        private double lat;
        private double lng;

        public TrackPoint() {
        }

        public TrackPoint(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }
}