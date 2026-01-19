package com.example.bustrackerpassenger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.bustrackerpassenger.models.Bus;
import com.example.bustrackerpassenger.models.BusLocation;
import com.example.bustrackerpassenger.utils.DistanceCalculator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MainActivity - Optimized Version
 * ✅ Modern Google Maps behavior
 * ✅ Compact info window (tidak menutupi layar)
 * ✅ Planned route BIRU, Actual track HIJAU
 * ✅ Toast "Loaded X buses" hanya sekali
 * ✅ Map style sama dengan dashboard JSX (light & soft)
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String FIREBASE_URL = "https://buskrutracker-default-rtdb.asia-southeast1.firebasedatabase.app";

    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;

    // UI Components
    private TextInputEditText searchInput;
    private MaterialCardView bottomSheet;
    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;

    // Bottom Sheet Views
    private TextView tvBusName, tvPlateNumber, tvBusClass, tvRoute, tvDriver,
            tvSpeed, tvETA, tvUserDistance, tvAvailableSeats, tvKondisi;
    private TextView tvETATime, tvETADistance, tvETADuration;
    private TextView tvTotalDistance, tvKondisiUpdate, tvLastUpdate;

    // Data
    private Map<String, Marker> busMarkers = new HashMap<>();
    private Map<String, Bus> busDataMap = new HashMap<>();
    private List<Bus> allBuses = new ArrayList<>();
    private Bus selectedBus;

    // Polylines
    private Polyline plannedRoutePolyline = null;
    private Polyline actualTrackPolyline = null;
    private Polyline userDistanceLine = null;
    private String currentSelectedBusId = null;

    // ✅ Flag untuk toast hanya sekali
    private boolean hasShownInitialToast = false;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("buses");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        requestLocationPermission();
        setupSearch();
    }

    private void initViews() {
        searchInput = findViewById(R.id.searchInput);
        bottomSheet = findViewById(R.id.bottomSheet);

        // Basic info
        tvBusName = findViewById(R.id.tvBusName);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        tvBusClass = findViewById(R.id.tvBusClass);
        tvRoute = findViewById(R.id.tvRoute);
        tvDriver = findViewById(R.id.tvDriver);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvETA = findViewById(R.id.tvETA);
        tvUserDistance = findViewById(R.id.tvUserDistance);
        tvAvailableSeats = findViewById(R.id.tvAvailableSeats);
        tvKondisi = findViewById(R.id.tvKondisi);

        // ETA Detail & Additional Info (with null safety)
        try {
            tvETATime = findViewById(R.id.tvETATime);
            tvETADistance = findViewById(R.id.tvETADistance);
            tvETADuration = findViewById(R.id.tvETADuration);
            tvTotalDistance = findViewById(R.id.tvTotalDistance);
            tvKondisiUpdate = findViewById(R.id.tvKondisiUpdate);
            tvLastUpdate = findViewById(R.id.tvLastUpdate);
        } catch (Exception e) {
            // Views not available
        }

        // Bottom Sheet: Default HIDDEN
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setPeekHeight(0);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    resetMapState();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        findViewById(R.id.btnFilter).setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            showFilterDialog();
        });

        findViewById(R.id.fabMyLocation).setOnClickListener(v -> moveToMyLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // ✅ Apply map style sama dengan dashboard JSX
        applyDashboardMapStyle();

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        enableMyLocation();

        // ✅ Langsung ke lokasi user saat app dibuka
        moveToUserLocationOnStart();

        // ✅ CUSTOM INFO WINDOW - Compact version
        mMap.setInfoWindowAdapter(new CompactInfoWindowAdapter());

        // ✅ Marker click: Show info window + zoom + polylines
        mMap.setOnMarkerClickListener(marker -> {
            String busId = (String) marker.getTag();
            if (busId != null) {
                selectedBus = busDataMap.get(busId);
                if (selectedBus != null) {
                    onBusMarkerClicked(selectedBus, marker);
                }
            }
            // ✅ Return true agar tidak auto-show info window
            // Info window akan di-show setelah zoom selesai (di callback)
            return true;
        });

        // ✅ Info window click: Expand bottom sheet
        mMap.setOnInfoWindowClickListener(marker -> {
            if (selectedBus != null) {
                updateBottomSheetInfo(selectedBus);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // ✅ Map click: Reset semua (modern behavior)
        mMap.setOnMapClickListener(latLng -> resetMapState());

        loadBusData();
    }

    /**
     * ✅ Apply Dashboard JSX Map Style - Light & Soft
     */
    private void applyDashboardMapStyle() {
        try {
            String mapStyle = "[\n" +
                    "  {\"featureType\": \"all\", \"elementType\": \"geometry\", \"stylers\": [{\"color\": \"#f5f5f5\"}]},\n" +
                    "  {\"featureType\": \"water\", \"elementType\": \"geometry\", \"stylers\": [{\"color\": \"#c9e9f6\"}]},\n" +
                    "  {\"featureType\": \"water\", \"elementType\": \"labels.text.fill\", \"stylers\": [{\"color\": \"#9e9e9e\"}]},\n" +
                    "  {\"featureType\": \"road\", \"elementType\": \"geometry\", \"stylers\": [{\"color\": \"#ffffff\"}]},\n" +
                    "  {\"featureType\": \"road\", \"elementType\": \"geometry.stroke\", \"stylers\": [{\"color\": \"#d9d9d9\"}]},\n" +
                    "  {\"featureType\": \"road.highway\", \"elementType\": \"geometry\", \"stylers\": [{\"color\": \"#fef5e0\"}]},\n" +
                    "  {\"featureType\": \"road.highway\", \"elementType\": \"geometry.stroke\", \"stylers\": [{\"color\": \"#f5d89f\"}]},\n" +
                    "  {\"featureType\": \"poi\", \"stylers\": [{\"visibility\": \"off\"}]},\n" +
                    "  {\"featureType\": \"transit\", \"stylers\": [{\"visibility\": \"off\"}]},\n" +
                    "  {\"featureType\": \"administrative.land_parcel\", \"stylers\": [{\"visibility\": \"off\"}]},\n" +
                    "  {\"featureType\": \"administrative.neighborhood\", \"stylers\": [{\"visibility\": \"off\"}]},\n" +
                    "  {\"featureType\": \"landscape.man_made\", \"elementType\": \"geometry.fill\", \"stylers\": [{\"color\": \"#f0f0f0\"}]},\n" +
                    "  {\"featureType\": \"landscape.natural\", \"elementType\": \"geometry.fill\", \"stylers\": [{\"color\": \"#e8f5e9\"}]}\n" +
                    "]";

            mMap.setMapStyle(new com.google.android.gms.maps.model.MapStyleOptions(mapStyle));
        } catch (Exception e) {
            // Use default style
        }
    }

    /**
     * ✅ COMPACT INFO WINDOW ADAPTER - Web Admin Style
     * Kotak kecil, compact, dan fixed size seperti dashboard web
     */
    private class CompactInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            String busId = (String) marker.getTag();
            if (busId == null) return null;

            Bus bus = busDataMap.get(busId);
            if (bus == null) return null;

            // ✅ Create compact info window (web admin style)
            View view = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.custom_bus_info_window, null);

            try {
                TextView tvInfoBusName = view.findViewById(R.id.tvInfoBusName);
                TextView tvInfoPlateClass = view.findViewById(R.id.tvInfoPlateClass);
                TextView tvInfoPassengers = view.findViewById(R.id.tvInfoPassengers);
                TextView tvInfoSpeed = view.findViewById(R.id.tvInfoSpeed);
                TextView tvInfoTotalDistance = view.findViewById(R.id.tvInfoTotalDistance);

                // Bus Name (max 15 chars)
                if (tvInfoBusName != null) {
                    String busName = bus.getDisplayName();
                    if (busName.length() > 15) busName = busName.substring(0, 12) + "...";
                    tvInfoBusName.setText(busName);
                }

                // Plate + Class combined
                if (tvInfoPlateClass != null) {
                    String plate = bus.getPlateNumber() != null ? bus.getPlateNumber() : "-";
                    String busClass = bus.getBusClass() != null ? bus.getBusClass().toUpperCase() : "REG";
                    if (busClass.length() > 3) busClass = busClass.substring(0, 3);

                    tvInfoPlateClass.setText(plate + " • " + busClass);
                }

                // Penumpang dengan percentage
                if (tvInfoPassengers != null) {
                    int occupancy = bus.getCurrentPassengers();
                    int capacity = bus.getCapacity();
                    int percentage = capacity > 0 ? Math.round((occupancy * 100f) / capacity) : 0;

                    tvInfoPassengers.setText(String.format("%d/%d (%d%%)",
                            occupancy, capacity, percentage));
                }

                // Kecepatan
                if (tvInfoSpeed != null) {
                    float speed = bus.getLocation() != null ? bus.getLocation().getSpeed() : 0;
                    tvInfoSpeed.setText(String.format("%.1f km/jam", speed));
                }

                // Total Distance
                if (tvInfoTotalDistance != null) {
                    Double totalDist = bus.getTotalDistance();
                    if (totalDist != null) {
                        tvInfoTotalDistance.setText(String.format("%.2f km", totalDist));
                    } else {
                        tvInfoTotalDistance.setText("0 km");
                    }
                }

                return view;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    /**
     * ✅ ON BUS MARKER CLICKED - Zoom moderat seperti "My Location"
     */
    private void onBusMarkerClicked(Bus bus, Marker marker) {
        selectedBus = bus;
        currentSelectedBusId = bus.getBusId();

        // Clear previous polylines
        clearAllPolylines();

        // Update bottom sheet data (tapi belum expand)
        updateBottomSheetInfo(bus);

        // ✅ Draw PLANNED route (BIRU)
        if (bus.getEncodedRoute() != null && !bus.getEncodedRoute().isEmpty()) {
            drawPlannedRoute(bus.getEncodedRoute());
        }

        // ✅ Draw ACTUAL track (HIJAU)
        drawActualTrack(bus.getTrack());

        // Draw user distance line
        if (userLocation != null) {
            drawUserDistanceLine(userLocation, bus.getLocation());
        }

        // ✅ Zoom level 15 (sama seperti "My Location" button)
        LatLng busPos = new LatLng(bus.getLocation().getLatitude(), bus.getLocation().getLongitude());
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(busPos, 15),
                800,
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (marker != null) {
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancel() {
                        if (marker != null) {
                            marker.showInfoWindow();
                        }
                    }
                }
        );
    }

    /**
     * ✅ Smart zoom berdasarkan context (track, route, user location)
     */
    private void zoomToBusWithContext(Bus bus, Marker marker) {
        if (mMap == null || bus.getLocation() == null) return;

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean hasPoints = false;

            // Include bus position
            LatLng busPos = new LatLng(bus.getLocation().getLatitude(), bus.getLocation().getLongitude());
            builder.include(busPos);
            hasPoints = true;

            // Include user location if available
            if (userLocation != null) {
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
            }

            // Include track points if available (max 10 points untuk optimal zoom)
            List<Bus.TrackPoint> trackPoints = bus.getTrack();
            if (trackPoints != null && !trackPoints.isEmpty()) {
                int step = Math.max(1, trackPoints.size() / 10);
                for (int i = 0; i < trackPoints.size(); i += step) {
                    Bus.TrackPoint tp = trackPoints.get(i);
                    builder.include(new LatLng(tp.getLat(), tp.getLng()));
                }
            }

            if (hasPoints) {
                LatLngBounds bounds = builder.build();

                // ✅ Padding 200px agar info window tidak terpotong
                int padding = 200;

                // Smooth animation dengan duration 800ms
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, padding),
                        800,
                        new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                // ✅ Show info window setelah zoom selesai
                                if (marker != null) {
                                    marker.showInfoWindow();
                                }
                            }

                            @Override
                            public void onCancel() {
                                // Still show info window
                                if (marker != null) {
                                    marker.showInfoWindow();
                                }
                            }
                        }
                );
            } else {
                // Fallback: zoom ke bus dengan level 14
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(busPos, 14),
                        800,
                        null
                );
            }

        } catch (Exception e) {
            // Fallback: simple zoom
            if (bus.getLocation() != null) {
                LatLng busPos = new LatLng(bus.getLocation().getLatitude(), bus.getLocation().getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busPos, 14), 800, null);
            }
        }
    }

    /**
     * ✅ RESET MAP STATE - Modern behavior
     */
    private void resetMapState() {
        clearAllPolylines();

        // Close all info windows
        for (Marker marker : busMarkers.values()) {
            if (marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
            }
        }

        selectedBus = null;
        currentSelectedBusId = null;

        // Hide bottom sheet
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void loadBusData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBuses.clear();
                int busCount = 0;

                for (DataSnapshot busSnapshot : snapshot.getChildren()) {
                    try {
                        String busId = busSnapshot.getKey();

                        String namaBus = busSnapshot.child("namaBus").getValue(String.class);
                        String plateNumber = busSnapshot.child("plateNumber").getValue(String.class);
                        String busClass = busSnapshot.child("class").getValue(String.class);
                        String route = busSnapshot.child("route").getValue(String.class);
                        Integer capacity = busSnapshot.child("capacity").getValue(Integer.class);
                        Integer currentPassengers = busSnapshot.child("currentPassengers").getValue(Integer.class);
                        String driver = busSnapshot.child("driver").getValue(String.class);
                        String status = busSnapshot.child("status").getValue(String.class);
                        String kondisi = busSnapshot.child("kondisi").getValue(String.class);
                        String kondisiUpdate = busSnapshot.child("kondisiUpdate").getValue(String.class);
                        Double totalDistance = busSnapshot.child("totalDistance").getValue(Double.class);
                        String encodedRoute = busSnapshot.child("routePolyline").getValue(String.class);

                        Double lat = busSnapshot.child("location/latitude").getValue(Double.class);
                        Double lng = busSnapshot.child("location/longitude").getValue(Double.class);
                        Double speed = busSnapshot.child("location/speed").getValue(Double.class);
                        String lastUpdate = busSnapshot.child("location/lastUpdate").getValue(String.class);

                        Bus.ETA eta = null;
                        DataSnapshot etaSnapshot = busSnapshot.child("eta");
                        if (etaSnapshot.exists()) {
                            Double remainingDistance = etaSnapshot.child("remainingDistance").getValue(Double.class);
                            Integer remainingTime = etaSnapshot.child("remainingTime").getValue(Integer.class);
                            String estimatedArrival = etaSnapshot.child("estimatedArrival").getValue(String.class);
                            eta = new Bus.ETA(remainingDistance, remainingTime, estimatedArrival);
                        }

                        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                            BusLocation location = new BusLocation(lat, lng,
                                    speed != null ? speed.floatValue() : 0, lastUpdate);

                            Bus bus = new Bus(busId, namaBus, plateNumber, busClass, route,
                                    capacity != null ? capacity : 0,
                                    currentPassengers != null ? currentPassengers : 0,
                                    driver, status, location);

                            bus.setKondisi(kondisi);
                            bus.setKondisiUpdate(kondisiUpdate);
                            bus.setTotalDistance(totalDistance);
                            bus.setEncodedRoute(encodedRoute);
                            bus.setEta(eta);

                            List<Bus.TrackPoint> trackPoints = new ArrayList<>();
                            DataSnapshot trackSnapshot = busSnapshot.child("track");
                            for (DataSnapshot trackPoint : trackSnapshot.getChildren()) {
                                Double trackLat = trackPoint.child("lat").getValue(Double.class);
                                Double trackLng = trackPoint.child("lng").getValue(Double.class);
                                if (trackLat != null && trackLng != null) {
                                    trackPoints.add(new Bus.TrackPoint(trackLat, trackLng));
                                }
                            }
                            bus.setTrack(trackPoints);

                            allBuses.add(bus);
                            busDataMap.put(busId, bus);
                            busCount++;

                            updateBusMarker(bus);

                            // Update realtime if bus is selected
                            if (busId.equals(currentSelectedBusId)) {
                                drawActualTrack(bus.getTrack());

                                if (userLocation != null) {
                                    drawUserDistanceLine(userLocation, bus.getLocation());
                                }

                                Marker marker = busMarkers.get(busId);
                                if (marker != null && marker.isInfoWindowShown()) {
                                    marker.showInfoWindow();
                                }

                                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                                    updateBottomSheetInfo(bus);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Skip error bus
                    }
                }

                // ✅ Toast hanya sekali saat pertama kali load
                if (!hasShownInitialToast && busCount > 0) {
                    Toast.makeText(MainActivity.this, "Loaded " + busCount + " buses", Toast.LENGTH_SHORT).show();
                    hasShownInitialToast = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BitmapDescriptor createCustomBusMarker(boolean isOnline) {
        int size = 120;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Shadow
        paint.setColor(Color.parseColor("#33000000"));
        canvas.drawCircle(size / 2f, size / 2f + 6, 54, paint);

        // White circle background
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, 54, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.parseColor("#E5E7EB"));
        canvas.drawCircle(size / 2f, size / 2f, 54, paint);
        paint.setStyle(Paint.Style.FILL);

        // Bus icon color
        int busColor = isOnline ? Color.parseColor("#3B82F6") : Color.parseColor("#9CA3AF");

        // Bus body
        paint.setColor(busColor);
        paint.setAlpha(50);
        canvas.drawRoundRect(30, 35, 90, 85, 8, 8, paint);

        paint.setAlpha(255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawRoundRect(30, 35, 90, 85, 8, 8, paint);
        paint.setStyle(Paint.Style.FILL);

        // Windows
        paint.setAlpha(80);
        canvas.drawRoundRect(35, 40, 55, 55, 4, 4, paint);
        canvas.drawRoundRect(65, 40, 85, 55, 4, 4, paint);

        // Lines
        paint.setAlpha(255);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(30, 60, 90, 60, paint);
        canvas.drawLine(30, 72, 90, 72, paint);

        // Wheels
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(42, 90, 8, paint);
        canvas.drawCircle(78, 90, 8, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(42, 90, 4, paint);
        canvas.drawCircle(78, 90, 4, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void updateBusMarker(Bus bus) {
        if (mMap == null) return;

        String busId = bus.getBusId();
        LatLng position = new LatLng(
                bus.getLocation().getLatitude(),
                bus.getLocation().getLongitude()
        );

        boolean isOnline = bus.getLocation().getSpeed() > 0 ||
                (bus.getStatus() != null && bus.getStatus().equals("active"));

        BitmapDescriptor icon = createCustomBusMarker(isOnline);

        if (busMarkers.containsKey(busId)) {
            Marker marker = busMarkers.get(busId);
            if (marker != null) {
                marker.setPosition(position);
                marker.setIcon(icon);
            }
        } else {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(bus.getDisplayName())
                    .snippet(bus.getRoute())
                    .icon(icon)
                    .anchor(0.5f, 0.5f);

            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(busId);
                busMarkers.put(busId, marker);
            }
        }
    }

    private void updateBottomSheetInfo(Bus bus) {
        // Basic Info
        tvBusName.setText(bus.getDisplayName());
        tvPlateNumber.setText(bus.getPlateNumber() != null ? bus.getPlateNumber() : "-");

        String busClass = bus.getBusClass() != null ? bus.getBusClass().toUpperCase() : "REGULER";
        tvBusClass.setText(busClass);

        tvRoute.setText(bus.getRoute() != null ? bus.getRoute() : "N/A");
        tvDriver.setText(bus.getDriver() != null ? bus.getDriver() : "N/A");

        float speed = bus.getLocation() != null ? bus.getLocation().getSpeed() : 0;
        tvSpeed.setText(String.format("%.0f km/j", speed));

        int available = bus.getAvailableSeats();
        tvAvailableSeats.setText(String.valueOf(available));

        // Kondisi
        if (bus.getKondisi() != null && tvKondisi != null) {
            String kondisi = bus.getKondisi().toUpperCase();
            tvKondisi.setText(kondisi);
            tvKondisi.setVisibility(View.VISIBLE);

            switch (bus.getKondisi().toLowerCase()) {
                case "lancar":
                    tvKondisi.setTextColor(Color.parseColor("#4CAF50"));
                    break;
                case "macet":
                    tvKondisi.setTextColor(Color.parseColor("#F44336"));
                    break;
                case "mogok":
                    tvKondisi.setTextColor(Color.parseColor("#FF9800"));
                    break;
            }
        }

        if (bus.getKondisiUpdate() != null && tvKondisiUpdate != null) {
            tvKondisiUpdate.setText("Update: " + getTimeAgo(bus.getKondisiUpdate()));
        }

        if (tvTotalDistance != null) {
            if (bus.getTotalDistance() != null) {
                tvTotalDistance.setText(String.format("%.1f km", bus.getTotalDistance()));
            } else {
                tvTotalDistance.setText("0 km");
            }
        }

        // User Distance & ETA
        if (userLocation != null && bus.getLocation() != null) {
            float distanceKm = DistanceCalculator.calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    bus.getLocation().getLatitude(), bus.getLocation().getLongitude()
            );

            tvUserDistance.setText(String.format("%.1f", distanceKm));

            float busSpeed = bus.getLocation().getSpeed();
            if (busSpeed > 5) {
                float etaMinutes = (distanceKm / busSpeed) * 60;
                tvETA.setText(String.format("%.0f", etaMinutes));
            } else {
                if (bus.getEta() != null && bus.getEta().getRemainingTime() != null) {
                    int etaMinutes = bus.getEta().getRemainingTime() / 60;
                    tvETA.setText(String.valueOf(etaMinutes));
                } else {
                    tvETA.setText("-");
                }
            }
        } else {
            tvUserDistance.setText("-");
            tvETA.setText("-");
        }

        // ETA Detail
        if (bus.getEta() != null) {
            Bus.ETA eta = bus.getEta();

            if (tvETATime != null) {
                if (eta.getEstimatedArrival() != null) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        Date arrivalDate = inputFormat.parse(eta.getEstimatedArrival());

                        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.US);
                        String arrivalTime = outputFormat.format(arrivalDate);
                        tvETATime.setText(arrivalTime);
                    } catch (Exception e) {
                        tvETATime.setText("-");
                    }
                } else {
                    tvETATime.setText("-");
                }
            }

            if (tvETADistance != null) {
                if (eta.getRemainingDistance() != null) {
                    tvETADistance.setText(String.format("%.1f km", eta.getRemainingDistance()));
                } else {
                    tvETADistance.setText("-");
                }
            }

            if (tvETADuration != null) {
                if (eta.getRemainingTime() != null) {
                    int minutes = eta.getRemainingTime() / 60;
                    tvETADuration.setText(String.format("%d menit", minutes));
                } else {
                    tvETADuration.setText("-");
                }
            }
        } else {
            if (tvETATime != null) tvETATime.setText("-");
            if (tvETADistance != null) tvETADistance.setText("-");
            if (tvETADuration != null) tvETADuration.setText("-");
        }

        if (tvLastUpdate != null) {
            if (bus.getLocation() != null && bus.getLocation().getLastUpdate() != null) {
                String timeAgo = getTimeAgo(bus.getLocation().getLastUpdate());
                String lastUpdateText = "Data real-time • Update " + timeAgo;
                tvLastUpdate.setText(lastUpdateText);
            } else {
                tvLastUpdate.setText("Data real-time");
            }
        }
    }

    /**
     * ✅ Draw PLANNED route - WARNA BIRU (seperti dashboard JSX)
     */
    private void drawPlannedRoute(String encodedPolyline) {
        if (plannedRoutePolyline != null) {
            plannedRoutePolyline.remove();
            plannedRoutePolyline = null;
        }

        if (encodedPolyline == null || encodedPolyline.isEmpty()) return;

        try {
            List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);
            if (decodedPath.isEmpty()) return;

            // ✅ PLANNED ROUTE = BIRU (#2196F3)
            PolylineOptions options = new PolylineOptions()
                    .addAll(decodedPath)
                    .width(8f)
                    .color(0xCC2196F3) // BIRU dengan opacity
                    .geodesic(true)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .zIndex(1);

            plannedRoutePolyline = mMap.addPolyline(options);

        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * ✅ Draw ACTUAL track - WARNA HIJAU (seperti dashboard JSX)
     */
    private void drawActualTrack(List<Bus.TrackPoint> trackPoints) {
        if (actualTrackPolyline != null) {
            actualTrackPolyline.remove();
            actualTrackPolyline = null;
        }

        if (trackPoints == null || trackPoints.isEmpty()) return;

        List<LatLng> points = new ArrayList<>();
        for (Bus.TrackPoint tp : trackPoints) {
            points.add(new LatLng(tp.getLat(), tp.getLng()));
        }

        if (points.size() < 2) return;

        // ✅ ACTUAL TRACK = HIJAU (#10B981)
        PolylineOptions options = new PolylineOptions()
                .addAll(points)
                .width(8f)
                .color(0xE610B981) // HIJAU dengan opacity
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .zIndex(10);

        actualTrackPolyline = mMap.addPolyline(options);
    }

    private void drawUserDistanceLine(Location userLoc, BusLocation busLoc) {
        if (userDistanceLine != null) {
            userDistanceLine.remove();
            userDistanceLine = null;
        }

        if (userLoc == null || busLoc == null) return;

        LatLng userPos = new LatLng(userLoc.getLatitude(), userLoc.getLongitude());
        LatLng busPos = new LatLng(busLoc.getLatitude(), busLoc.getLongitude());

        PolylineOptions options = new PolylineOptions()
                .add(userPos, busPos)
                .width(6f)
                .color(0xAA9E9E9E)
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .zIndex(5);

        userDistanceLine = mMap.addPolyline(options);
    }

    private void clearAllPolylines() {
        if (actualTrackPolyline != null) {
            actualTrackPolyline.remove();
            actualTrackPolyline = null;
        }
        if (plannedRoutePolyline != null) {
            plannedRoutePolyline.remove();
            plannedRoutePolyline = null;
        }
        if (userDistanceLine != null) {
            userDistanceLine.remove();
            userDistanceLine = null;
        }
    }

    private String getTimeAgo(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(timestamp);

            if (date == null) return "N/A";

            long timeInMillis = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - timeInMillis;

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (seconds < 60) {
                return seconds + " detik lalu";
            } else if (minutes < 60) {
                return minutes + " menit lalu";
            } else if (hours < 24) {
                return hours + " jam lalu";
            } else {
                return days + " hari lalu";
            }
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBuses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterBuses(String query) {
        if (query.isEmpty()) {
            for (Bus bus : allBuses) {
                Marker marker = busMarkers.get(bus.getBusId());
                if (marker != null) marker.setVisible(true);
            }
            return;
        }

        query = query.toLowerCase();

        for (Bus bus : allBuses) {
            boolean matches = (bus.getNamaBus() != null && bus.getNamaBus().toLowerCase().contains(query)) ||
                    (bus.getPlateNumber() != null && bus.getPlateNumber().toLowerCase().contains(query)) ||
                    (bus.getRoute() != null && bus.getRoute().toLowerCase().contains(query)) ||
                    (bus.getBusClass() != null && bus.getBusClass().toLowerCase().contains(query));

            Marker marker = busMarkers.get(bus.getBusId());
            if (marker != null) marker.setVisible(matches);
        }
    }

    private void showFilterDialog() {
        String[] options = {"Semua Bus", "Ekonomi", "Eksekutif", "VIP", "Bus dengan Kursi Tersedia"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Bus");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: filterByClass(null); break;
                case 1: filterByClass("Ekonomi"); break;
                case 2: filterByClass("Eksekutif"); break;
                case 3: filterByClass("VIP"); break;
                case 4: filterByAvailability(); break;
            }
        });
        builder.show();
    }

    private void filterByClass(String busClass) {
        for (Bus bus : allBuses) {
            Marker marker = busMarkers.get(bus.getBusId());
            if (marker != null) {
                if (busClass == null) {
                    marker.setVisible(true);
                } else {
                    boolean matches = bus.getBusClass() != null && bus.getBusClass().equalsIgnoreCase(busClass);
                    marker.setVisible(matches);
                }
            }
        }
    }

    private void filterByAvailability() {
        for (Bus bus : allBuses) {
            Marker marker = busMarkers.get(bus.getBusId());
            if (marker != null) {
                boolean hasSeats = bus.getAvailableSeats() > 0;
                marker.setVisible(hasSeats);
            }
        }
    }

    private void moveToMyLocation() {
        if (userLocation != null && mMap != null) {
            LatLng myLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
            Toast.makeText(this, "Lokasi Anda", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show();
            getUserLocation();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getUserLocation();
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    userLocation = location;

                    if (selectedBus != null) {
                        drawUserDistanceLine(userLocation, selectedBus.getLocation());

                        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            updateBottomSheetInfo(selectedBus);
                        }
                    }
                }
            });
        }
    }

    /**
     * ✅ Move to user location saat app pertama kali dibuka
     */
    private void moveToUserLocationOnStart() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && mMap != null) {
                    userLocation = location;
                    LatLng userPos = new LatLng(location.getLatitude(), location.getLongitude());

                    // ✅ Smooth animation ke lokasi user dengan zoom 15
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPos, 15), 1000, null);
                } else {
                    // ✅ Fallback ke Madiun jika lokasi tidak tersedia
                    LatLng madiun = new LatLng(-7.6298, 111.5239);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madiun, 12));
                }
            }).addOnFailureListener(e -> {
                // ✅ Jika gagal, fallback ke Madiun
                LatLng madiun = new LatLng(-7.6298, 111.5239);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madiun, 12));
            });
        } else {
            // ✅ Jika permission belum diberikan, fallback ke Madiun
            LatLng madiun = new LatLng(-7.6298, 111.5239);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madiun, 12));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getUserLocation();

                // ✅ Setelah permission granted, langsung move ke lokasi user
                if (mMap != null) {
                    moveToUserLocationOnStart();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }
}