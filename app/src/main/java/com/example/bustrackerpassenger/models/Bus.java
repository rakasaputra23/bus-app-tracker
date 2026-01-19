package com.example.bustrackerpassenger.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Bus Model - Updated untuk sync dengan Firebase dari Kru App
 * ⭐ ADDED: namaBus field
 */
public class Bus {
    private String busId;
    private String namaBus;           // ⭐ FIELD BARU
    private String plateNumber;
    private String busClass;
    private String route;
    private int capacity;
    private int currentPassengers;
    private String driver;
    private String status;
    private String kondisi;           // ⭐ FIELD BARU (lancar/macet/mogok)
    private String kondisiUpdate;     // ⭐ FIELD BARU
    private Double totalDistance;     // ⭐ FIELD BARU
    private BusLocation location;
    private List<TrackPoint> track;
    private String encodedRoute;      // routePolyline dari Firebase
    private ETA eta;                  // ⭐ FIELD BARU

    // Empty constructor - WAJIB untuk Firebase
    public Bus() {
        this.track = new ArrayList<>();
    }

    // Constructor dengan parameter
    public Bus(String busId, String namaBus, String plateNumber, String busClass,
               String route, int capacity, int currentPassengers, String driver,
               String status, BusLocation location) {
        this.busId = busId;
        this.namaBus = namaBus;
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

    // ===== GETTERS & SETTERS =====

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getNamaBus() {
        return namaBus;
    }

    public void setNamaBus(String namaBus) {
        this.namaBus = namaBus;
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

    public String getKondisi() {
        return kondisi;
    }

    public void setKondisi(String kondisi) {
        this.kondisi = kondisi;
    }

    public String getKondisiUpdate() {
        return kondisiUpdate;
    }

    public void setKondisiUpdate(String kondisiUpdate) {
        this.kondisiUpdate = kondisiUpdate;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
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

    public String getEncodedRoute() {
        return encodedRoute;
    }

    public void setEncodedRoute(String encodedRoute) {
        this.encodedRoute = encodedRoute;
    }

    public ETA getEta() {
        return eta;
    }

    public void setEta(ETA eta) {
        this.eta = eta;
    }

    // ===== HELPER METHODS =====

    public int getAvailableSeats() {
        return capacity - currentPassengers;
    }

    public String getOccupancyText() {
        return currentPassengers + "/" + capacity;
    }

    /**
     * Get display name (prioritas namaBus, fallback ke plateNumber)
     */
    public String getDisplayName() {
        if (namaBus != null && !namaBus.isEmpty()) {
            return namaBus;
        }
        return plateNumber != null ? plateNumber : "Unknown";
    }

    // ===== INNER CLASSES =====

    /**
     * Track Point untuk actual track
     */
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

    /**
     * ETA Information
     */
    public static class ETA {
        private Double remainingDistance;
        private Integer remainingTime;
        private String estimatedArrival;

        public ETA() {
        }

        public ETA(Double remainingDistance, Integer remainingTime, String estimatedArrival) {
            this.remainingDistance = remainingDistance;
            this.remainingTime = remainingTime;
            this.estimatedArrival = estimatedArrival;
        }

        public Double getRemainingDistance() {
            return remainingDistance;
        }

        public void setRemainingDistance(Double remainingDistance) {
            this.remainingDistance = remainingDistance;
        }

        public Integer getRemainingTime() {
            return remainingTime;
        }

        public void setRemainingTime(Integer remainingTime) {
            this.remainingTime = remainingTime;
        }

        public String getEstimatedArrival() {
            return estimatedArrival;
        }

        public void setEstimatedArrival(String estimatedArrival) {
            this.estimatedArrival = estimatedArrival;
        }
    }
}