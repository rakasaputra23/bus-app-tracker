package com.example.bustrackerpassenger.utils;

import android.location.Location;

public class DistanceCalculator {

    // Hitung jarak antara dua koordinat (dalam km)
    public static float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        return loc1.distanceTo(loc2) / 1000; // Convert to kilometers
    }

    // Estimasi waktu kedatangan (ETA) berdasarkan jarak dan kecepatan
    public static String calculateETA(float distanceKm, float speedKmh) {
        if (speedKmh <= 0) {
            return "Bus berhenti";
        }

        float hours = distanceKm / speedKmh;
        int minutes = Math.round(hours * 60);

        if (minutes < 1) {
            return "Kurang dari 1 menit";
        } else if (minutes < 60) {
            return minutes + " menit";
        } else {
            int hrs = minutes / 60;
            int mins = minutes % 60;
            return hrs + " jam " + mins + " menit";
        }
    }
}