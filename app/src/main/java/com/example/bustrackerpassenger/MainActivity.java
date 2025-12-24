package com.example.bustrackerpassenger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;

    // UI Components
    private TextInputEditText searchInput;
    private MaterialCardView bottomSheet;
    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;

    // Bottom Sheet Views
    private TextView tvPlateNumber, tvBusClass, tvRoute, tvDriver,
            tvOccupancy, tvSpeed, tvETA;
    private com.google.android.material.button.MaterialButton btnToggleView;

    // View mode: true = full route, false = actual track only
    private boolean isShowingFullRoute = false;

    // Data
    private Map<String, Marker> busMarkers = new HashMap<>();
    private Map<String, Bus> busDataMap = new HashMap<>();
    private List<Bus> allBuses = new ArrayList<>();
    private Bus selectedBus;

    // ===== MODIFIKASI: Ghost vs Real - Dua Polyline =====
    private Polyline plannedRoutePolyline = null;  // Garis Abu-abu (Planned Route)
    private Polyline actualTrackPolyline = null;   // Garis Biru (Actual Track)
    private String currentSelectedBusId = null;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance("https://bustrackerapp-3f25e-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("buses");

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Views
        initViews();

        // Setup Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Request Location Permission
        requestLocationPermission();

        // Setup Search
        setupSearch();
    }

    private void initViews() {
        searchInput = findViewById(R.id.searchInput);
        bottomSheet = findViewById(R.id.bottomSheet);

        // Bottom Sheet Views
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        tvBusClass = findViewById(R.id.tvBusClass);
        tvRoute = findViewById(R.id.tvRoute);
        tvDriver = findViewById(R.id.tvDriver);
        tvOccupancy = findViewById(R.id.tvOccupancy);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvETA = findViewById(R.id.tvETA);

        // ===== Toggle View Button =====
        btnToggleView = findViewById(R.id.btnToggleView);
        btnToggleView.setOnClickListener(v -> toggleViewMode());

        // Setup Bottom Sheet Behavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Listener untuk bottom sheet collapse
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Hapus polyline ketika bottom sheet disembunyikan
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    clearCurrentTrack();
                    isShowingFullRoute = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Not used
            }
        });

        // Filter Button
        findViewById(R.id.btnFilter).setOnClickListener(v -> showFilterDialog());

        // FAB My Location Button
        findViewById(R.id.fabMyLocation).setOnClickListener(v -> moveToMyLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set map style
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // DISABLE semua kontrol bawaan Google Maps untuk tampilan modern
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Aktifkan gesture yang smooth
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        // Set initial camera position (Indonesia)
        LatLng indonesia = new LatLng(-6.200000, 106.816666);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indonesia, 6));

        // Enable user location
        enableMyLocation();

        // Marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            String busId = (String) marker.getTag();
            if (busId != null) {
                selectedBus = busDataMap.get(busId);
                if (selectedBus != null) {
                    showBusDetails(selectedBus);
                }
            }
            return true;
        });

        // Listener untuk klik map (hapus polyline jika klik area kosong)
        mMap.setOnMapClickListener(latLng -> {
            // Sembunyikan bottom sheet dan hapus polyline
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                clearCurrentTrack();
            }
        });

        // Load bus data from Firebase
        loadBusData();
    }

    private void loadBusData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBuses.clear();

                for (DataSnapshot busSnapshot : snapshot.getChildren()) {
                    String busId = busSnapshot.getKey();

                    // Parse bus data
                    String plateNumber = busSnapshot.child("plateNumber").getValue(String.class);
                    String busClass = busSnapshot.child("class").getValue(String.class);
                    String route = busSnapshot.child("route").getValue(String.class);
                    Integer capacity = busSnapshot.child("capacity").getValue(Integer.class);
                    Integer currentPassengers = busSnapshot.child("currentPassengers").getValue(Integer.class);
                    String driver = busSnapshot.child("driver").getValue(String.class);
                    String status = busSnapshot.child("status").getValue(String.class);

                    // ===== Parse Encoded Route =====
                    String encodedRoute = busSnapshot.child("routePolyline").getValue(String.class);

                    // Parse location
                    Double lat = busSnapshot.child("location/latitude").getValue(Double.class);
                    Double lng = busSnapshot.child("location/longitude").getValue(Double.class);
                    Float speed = busSnapshot.child("location/speed").getValue(Float.class);
                    String lastUpdate = busSnapshot.child("location/lastUpdate").getValue(String.class);

                    if (lat != null && lng != null) {
                        BusLocation location = new BusLocation(lat, lng,
                                speed != null ? speed : 0, lastUpdate);

                        Bus bus = new Bus(busId, plateNumber, busClass, route,
                                capacity != null ? capacity : 0,
                                currentPassengers != null ? currentPassengers : 0,
                                driver, status, location);

                        // Set encoded route
                        bus.setEncodedRoute(encodedRoute);

                        // Parse track history
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

                        // Update marker
                        updateBusMarker(bus);

                        // ===== LOGIKA UPDATE REALTIME =====
                        // Jika bus ini sedang dipilih user, update polyline-nya secara real-time
                        if (busId.equals(currentSelectedBusId)) {
                            // Update garis jejak aktual (akan bertambah panjang otomatis)
                            drawActualTrack(bus.getTrack());

                            // Update info di bottom sheet
                            if (selectedBus != null && selectedBus.getBusId().equals(busId)) {
                                selectedBus = bus;
                                updateBottomSheetInfo(bus);
                            }
                        }
                    }
                }

                Toast.makeText(MainActivity.this,
                        "Loaded " + allBuses.size() + " buses", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== Menggambar Jalur Planned (Ghost Route) =====
    private void drawPlannedRoute(String encodedPolyline) {
        // Hapus garis lama jika ada
        if (plannedRoutePolyline != null) {
            plannedRoutePolyline.remove();
            plannedRoutePolyline = null;
        }

        if (encodedPolyline == null || encodedPolyline.isEmpty()) {
            return;
        }

        try {
            // Decode string encoded menjadi List LatLng
            List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);

            if (decodedPath.isEmpty()) {
                Toast.makeText(this, "Polyline kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Pattern putus-putus LEBIH BESAR agar terlihat
            List<PatternItem> pattern = Arrays.asList(
                    new Dash(30),   // Dash lebih panjang (dari 20 → 30)
                    new Gap(15)     // Gap lebih besar (dari 10 → 15)
            );

            // ✅ Gambar garis ORANGE/PINK agar lebih terlihat
            PolylineOptions options = new PolylineOptions()
                    .addAll(decodedPath)
                    .width(18f)                      // ✅ Lebih lebar (dari 14 → 18)
                    .color(0xDDFF6F00)               // ✅ Orange terang dengan opacity tinggi (0xDD = 87%)
                    .geodesic(true)
                    .pattern(pattern)                // Pattern putus-putus
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .zIndex(1);                      // Di bawah actual track

            plannedRoutePolyline = mMap.addPolyline(options);

            // ✅ Debug log
            Toast.makeText(this, "Planned route digambar: " + decodedPath.size() + " points",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error decoding route: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ===== ✅ PERBAIKAN: Menggambar Jalur Actual (Real Track) - SATU WARNA SAJA =====
    private void drawActualTrack(List<Bus.TrackPoint> trackPoints) {
        // Hapus polyline sebelumnya
        if (actualTrackPolyline != null) {
            actualTrackPolyline.remove();
            actualTrackPolyline = null;
        }

        if (trackPoints == null || trackPoints.isEmpty()) return;

        // Convert track points to LatLng
        List<LatLng> points = new ArrayList<>();
        for (Bus.TrackPoint tp : trackPoints) {
            points.add(new LatLng(tp.getLat(), tp.getLng()));
        }

        if (points.size() < 2) return;

        // ===== ✅ SATU WARNA UNTUK SEMUA BUS: Biru Solid =====
        int color = 0xFF2196F3; // Biru Material Design

        // Create polyline dengan style modern
        PolylineOptions options = new PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(color)
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .zIndex(10);  // Pastikan ini di atas garis abu-abu

        actualTrackPolyline = mMap.addPolyline(options);
    }

    private void updateBusMarker(Bus bus) {
        if (mMap == null) return;

        String busId = bus.getBusId();
        LatLng position = new LatLng(
                bus.getLocation().getLatitude(),
                bus.getLocation().getLongitude()
        );

        // Tentukan icon berdasarkan kecepatan
        int iconResource;
        if (bus.getLocation().getSpeed() < 5) {
            iconResource = R.drawable.ic_bus_stopped;
        } else if (bus.getLocation().getSpeed() < 30) {
            iconResource = R.drawable.ic_bus_slow;
        } else {
            iconResource = R.drawable.ic_bus_active;
        }

        // Convert vector to bitmap
        BitmapDescriptor icon = bitmapDescriptorFromVector(iconResource);

        if (busMarkers.containsKey(busId)) {
            // Update existing marker
            Marker marker = busMarkers.get(busId);
            if (marker != null) {
                marker.setPosition(position);
                marker.setIcon(icon);
            }
        } else {
            // Create new marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(bus.getPlateNumber())
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

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // ===== ✅ PERBAIKAN: showBusDetails dengan NULL SAFETY =====
    private void showBusDetails(Bus bus) {
        selectedBus = bus;
        currentSelectedBusId = bus.getBusId();

        // Update info di bottom sheet
        updateBottomSheetInfo(bus);

        // ===== ✅ VALIDASI NULL untuk encodedRoute =====
        if (bus.getEncodedRoute() != null && !bus.getEncodedRoute().isEmpty()) {
            // 1. Gambar Jalur Ghost (Planned Route)
            drawPlannedRoute(bus.getEncodedRoute());
        } else {
            // Optional: Notifikasi jika rute belum diset admin
            Toast.makeText(this, "Rute default belum tersedia dari admin",
                    Toast.LENGTH_SHORT).show();
        }

        // 2. Gambar Jalur Aktual (Real Track)
        drawActualTrack(bus.getTrack());

        // 3. ✅ Zoom ke KEDUA garis (planned + actual)
        zoomToTrack(bus);

        // Show bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    // ===== Helper untuk update info bottom sheet =====
    private void updateBottomSheetInfo(Bus bus) {
        tvPlateNumber.setText(bus.getPlateNumber());
        tvBusClass.setText(bus.getBusClass());
        tvRoute.setText(bus.getRoute());
        tvDriver.setText(bus.getDriver());
        tvSpeed.setText(String.format("%.0f km/jam", bus.getLocation().getSpeed()));

        // Occupancy
        int available = bus.getAvailableSeats();
        String occupancyText = available + " dari " + bus.getCapacity() + " kursi";
        tvOccupancy.setText(occupancyText);

        // Set color based on availability
        if (available < 5) {
            tvOccupancy.setTextColor(Color.parseColor("#D32F2F"));
        } else if (available < 15) {
            tvOccupancy.setTextColor(Color.parseColor("#F57C00"));
        } else {
            tvOccupancy.setTextColor(Color.parseColor("#388E3C"));
        }

        // Calculate ETA
        if (userLocation != null) {
            float distance = DistanceCalculator.calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    bus.getLocation().getLatitude(), bus.getLocation().getLongitude()
            );
            String eta = DistanceCalculator.calculateETA(distance, bus.getLocation().getSpeed());
            tvETA.setText(eta);
        } else {
            tvETA.setText("Lokasi tidak tersedia");
        }
    }

    // ===== ✅ SOLUSI BARU: Zoom dengan Include Sebagian Planned Route =====
    private void zoomToTrack(Bus bus) {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean hasPoints = false;

            // ===== 1. Include Actual Track points =====
            List<Bus.TrackPoint> trackPoints = bus.getTrack();
            if (trackPoints != null && !trackPoints.isEmpty()) {
                for (Bus.TrackPoint tp : trackPoints) {
                    builder.include(new LatLng(tp.getLat(), tp.getLng()));
                    hasPoints = true;
                }
            }

            // ===== 2. Include SEBAGIAN Planned Route (sample setiap 5 points) =====
            if (bus.getEncodedRoute() != null && !bus.getEncodedRoute().isEmpty()) {
                try {
                    List<LatLng> plannedPoints = PolyUtil.decode(bus.getEncodedRoute());

                    // Ambil sample points (tidak semua, agar zoom tidak terlalu jauh)
                    int sampleRate = Math.max(1, plannedPoints.size() / 20); // Ambil max 20 sample points

                    for (int i = 0; i < plannedPoints.size(); i += sampleRate) {
                        builder.include(plannedPoints.get(i));
                        hasPoints = true;
                    }

                    // Include juga first & last point planned route
                    if (!plannedPoints.isEmpty()) {
                        builder.include(plannedPoints.get(0)); // Start point
                        builder.include(plannedPoints.get(plannedPoints.size() - 1)); // End point
                    }

                } catch (Exception e) {
                    // Decode error, skip planned route dari zoom
                }
            }

            if (hasPoints) {
                LatLngBounds bounds = builder.build();
                int padding = 120; // Padding untuk UI
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1500, null);
            } else {
                // ===== Fallback: Zoom ke posisi bus dengan level moderat =====
                if (bus.getLocation() != null) {
                    LatLng busPosition = new LatLng(
                            bus.getLocation().getLatitude(),
                            bus.getLocation().getLongitude()
                    );
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busPosition, 11), 1500, null);
                }
            }

        } catch (Exception e) {
            // ===== Fallback jika error =====
            if (bus.getLocation() != null) {
                LatLng busPos = new LatLng(
                        bus.getLocation().getLatitude(),
                        bus.getLocation().getLongitude()
                );
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busPos, 11), 1500, null);
            }
        }
    }

    // ===== Method untuk clear KEDUA polyline =====
    private void clearCurrentTrack() {
        if (actualTrackPolyline != null) {
            actualTrackPolyline.remove();
            actualTrackPolyline = null;
        }
        if (plannedRoutePolyline != null) {
            plannedRoutePolyline.remove();
            plannedRoutePolyline = null;
        }
        currentSelectedBusId = null;
        selectedBus = null;
        isShowingFullRoute = false;
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBuses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterBuses(String query) {
        if (query.isEmpty()) {
            for (Bus bus : allBuses) {
                Marker marker = busMarkers.get(bus.getBusId());
                if (marker != null) {
                    marker.setVisible(true);
                }
            }
            return;
        }

        query = query.toLowerCase();

        for (Bus bus : allBuses) {
            boolean matches = bus.getPlateNumber().toLowerCase().contains(query) ||
                    bus.getRoute().toLowerCase().contains(query) ||
                    bus.getBusClass().toLowerCase().contains(query);

            Marker marker = busMarkers.get(bus.getBusId());
            if (marker != null) {
                marker.setVisible(matches);
            }
        }
    }

    private void showFilterDialog() {
        String[] options = {"Semua Bus", "Ekonomi", "Eksekutif", "VIP", "Bus dengan Kursi Tersedia"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Bus");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    filterByClass(null);
                    break;
                case 1:
                    filterByClass("Ekonomi");
                    break;
                case 2:
                    filterByClass("Eksekutif");
                    break;
                case 3:
                    filterByClass("VIP");
                    break;
                case 4:
                    filterByAvailability();
                    break;
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
                    boolean matches = bus.getBusClass().equalsIgnoreCase(busClass);
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
            LatLng myLocation = new LatLng(
                    userLocation.getLatitude(),
                    userLocation.getLongitude()
            );
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
            Toast.makeText(this, "Lokasi Anda", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show();
            getUserLocation();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                }
            });
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
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            clearCurrentTrack();
        } else {
            super.onBackPressed();
        }
    }

    // ===== TOGGLE VIEW MODE: Switch between Full Route vs Actual Track =====
    private void toggleViewMode() {
        if (selectedBus == null) return;

        isShowingFullRoute = !isShowingFullRoute;

        if (isShowingFullRoute) {
            // Mode: Show Full Planned Route
            btnToggleView.setText("Track Only");
            zoomToFullRoute(selectedBus);
            Toast.makeText(this, "Menampilkan seluruh rute", Toast.LENGTH_SHORT).show();
        } else {
            // Mode: Show Actual Track Only
            btnToggleView.setText("Full Route");
            zoomToActualTrack(selectedBus);
            Toast.makeText(this, "Fokus ke jejak bus", Toast.LENGTH_SHORT).show();
        }
    }

    // ===== Zoom ke FULL PLANNED ROUTE (seluruh rute dari admin) =====
    private void zoomToFullRoute(Bus bus) {
        if (bus.getEncodedRoute() == null || bus.getEncodedRoute().isEmpty()) {
            Toast.makeText(this, "Rute default tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<LatLng> plannedPoints = PolyUtil.decode(bus.getEncodedRoute());
            if (plannedPoints.isEmpty()) return;

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : plannedPoints) {
                builder.include(point);
            }

            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1500, null);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading full route", Toast.LENGTH_SHORT).show();
        }
    }

    // ===== Zoom ke ACTUAL TRACK saja =====
    private void zoomToActualTrack(Bus bus) {
        List<Bus.TrackPoint> trackPoints = bus.getTrack();

        if (trackPoints == null || trackPoints.isEmpty()) {
            // Jika tidak ada track, zoom ke posisi bus
            if (bus.getLocation() != null) {
                LatLng busPos = new LatLng(
                        bus.getLocation().getLatitude(),
                        bus.getLocation().getLongitude()
                );
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busPos, 12), 1500, null);
            }
            return;
        }

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Bus.TrackPoint tp : trackPoints) {
                builder.include(new LatLng(tp.getLat(), tp.getLng()));
            }

            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 1500, null);

        } catch (Exception e) {
            Toast.makeText(this, "Error zooming to track", Toast.LENGTH_SHORT).show();
        }
    }
}