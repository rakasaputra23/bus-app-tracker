# 🚌 Bus Tracker - Aplikasi Pelacakan Armada Bus Real-Time

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-21-blue.svg)](https://developer.android.com/about/versions/lollipop)
[![License](https://img.shields.io/badge/License-MIT-red.svg)](LICENSE)

Aplikasi Android untuk penumpang yang memungkinkan pelacakan posisi armada bus secara real-time, pencarian rute, informasi okupansi, dan estimasi waktu kedatangan.

## 📱 Tentang Aplikasi

**Bus Tracker** adalah modul aplikasi mobile berbasis Android Native (Java) yang dirancang khusus untuk calon penumpang dan masyarakat umum. Aplikasi ini menyediakan informasi real-time mengenai keberadaan armada bus, memudahkan pengguna dalam merencanakan perjalanan mereka dengan lebih efisien.

## ✨ Fitur Utama

### 1. 🗺️ Peta Sebaran Armada (All-Bus View)
- **Tampilan Otomatis**: Saat aplikasi dibuka, pengguna langsung disajikan peta digital yang menampilkan seluruh armada bus yang sedang beroperasi
- **Real-Time Tracking**: Posisi bus-bus diperbarui secara real-time dan dapat dilihat bergerak di peta
- **Visualisasi Langsung**: Tidak perlu melakukan pencarian terlebih dahulu untuk melihat ketersediaan bus
- **Interface Interaktif**: Peta yang mudah digunakan dengan zoom dan pan

### 2. 🔍 Pencarian dan Filter (Search Engine)
Fitur pencarian canggih untuk memfilter tampilan bus di peta berdasarkan:
- **Jurusan/Trayek** 
  - Contoh: "Jakarta - Bandung", "Surabaya - Malang"
  - Filter berdasarkan rute perjalanan
- **Kelas Bus**
  - Ekonomi
  - Eksekutif
- **Plat Nomor**
  - Pencarian bus spesifik berdasarkan nomor kendaraan

### 3. ℹ️ Detail Info & Okupansi
Ketika pengguna menekan salah satu ikon bus di peta, akan ditampilkan informasi lengkap:
- **Posisi Saat Ini**: Lokasi bus secara geografis
- **Rute**: Trayek perjalanan bus
- **Sisa Kursi**: Jumlah kursi yang masih tersedia
- **Jumlah Penumpang**: Informasi okupansi real-time

### 4. ⏱️ Estimasi Kedatangan (ETA)
- Menampilkan perkiraan waktu bus akan tiba di lokasi pengguna
- Kalkulasi berbasis GPS dan traffic conditions
- Update otomatis sesuai pergerakan bus
- Notifikasi ketika bus sudah dekat

## 🛠️ Teknologi yang Digunakan

| Kategori | Teknologi |
|----------|-----------|
| **Platform** | Android Native |
| **Bahasa Pemrograman** | Java |
| **IDE** | Android Studio |
| **Maps API** | Google Maps Android API |
| **Real-time Communication** | Firebase Realtime Database / WebSocket |
| **Location Services** | Google Play Services Location API |
| **Networking** | Retrofit 2 / OkHttp |
| **JSON Parsing** | Gson |
| **Dependency Injection** | Dagger/Hilt (Optional) |
| **Min SDK** | API 21 (Android 5.0 Lollipop) |
| **Target SDK** | API 34 (Android 14) |

## 📋 Prasyarat

Sebelum menjalankan aplikasi, pastikan Anda sudah menginstal:

- **Android Studio** - Arctic Fox atau versi lebih baru
- **JDK** - Java Development Kit 11 atau lebih tinggi
- **Android SDK** - API Level 21 hingga 34
- **Google Play Services** - Untuk Maps dan Location
- **Emulator Android** atau perangkat fisik untuk testing
- **API Keys** - Google Maps API Key (lihat bagian Konfigurasi)

## 🚀 Cara Instalasi

### 1. Clone Repository
```bash
git clone https://github.com/username/bus-tracker-app.git
cd bus-tracker-app
```

### 2. Buka di Android Studio
- Buka Android Studio
- Pilih **File → Open**
- Navigate ke folder project yang sudah di-clone
- Tunggu proses Gradle sync selesai

### 3. Konfigurasi API Keys

Buat file `local.properties` di root project (jika belum ada) dan tambahkan:

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
API_BASE_URL=YOUR_BACKEND_API_URL_HERE
```

Atau tambahkan di `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### 4. Sync Gradle
```bash
./gradlew clean build
```

### 5. Run Aplikasi
- Hubungkan perangkat Android via USB atau jalankan emulator
- Klik tombol **Run** (▶️) di Android Studio
- Pilih device target

## 📁 Struktur Project

```
bus-tracker-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yourpackage/bustracker/
│   │   │   │   ├── activities/
│   │   │   │   │   └── MainActivity.java
│   │   │   │   ├── adapters/
│   │   │   │   │   └── BusInfoAdapter.java
│   │   │   │   ├── models/
│   │   │   │   │   ├── Bus.java
│   │   │   │   │   ├── Route.java
│   │   │   │   │   └── Location.java
│   │   │   │   ├── services/
│   │   │   │   │   └── LocationService.java
│   │   │   │   ├── utils/
│   │   │   │   │   ├── MapHelper.java
│   │   │   │   │   └── ETACalculator.java
│   │   │   │   └── api/
│   │   │   │       ├── ApiClient.java
│   │   │   │       └── ApiService.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── values/
│   │   │   │   └── menu/
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle
├── gradle/
├── README.md
└── settings.gradle
```

## 🎨 Screenshots

| Peta Armada | Pencarian | Detail Bus | ETA |
|-------------|-----------|------------|-----|
| ![Map](screenshots/map.png) | ![Search](screenshots/search.png) | ![Detail](screenshots/detail.png) | ![ETA](screenshots/eta.png) |

## 🔧 Konfigurasi

### Dependencies (build.gradle)

```gradle
dependencies {
    // AndroidX
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Google Maps
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    implementation 'com.google.android.gms:play-services-location:21.1.0'
    
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Firebase (Optional untuk real-time)
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-database'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

### Permissions (AndroidManifest.xml)

```xml
<!-- Location Permissions -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Internet -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Foreground Service (untuk tracking) -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

## 📖 Cara Penggunaan

### Untuk End User

1. **Buka Aplikasi**
   - Aplikasi akan langsung menampilkan peta dengan semua bus aktif

2. **Lihat Posisi Bus**
   - Icon bus di peta menunjukkan posisi real-time
   - Warna berbeda untuk kelas bus yang berbeda

3. **Cari Bus Tertentu**
   - Gunakan search bar di bagian atas
   - Ketik jurusan (contoh: "Jakarta-Bandung")
   - Atau filter berdasarkan kelas bus

4. **Lihat Detail**
   - Tap pada icon bus di peta
   - Bottom sheet akan muncul dengan info lengkap
   - Lihat sisa kursi dan rute

5. **Cek ETA**
   - Aktifkan lokasi Anda
   - Aplikasi akan menghitung estimasi kedatangan
   - Lihat berapa menit lagi bus tiba

## 🔐 Keamanan & Privacy

- Lokasi pengguna hanya digunakan untuk kalkulasi ETA
- Data lokasi tidak disimpan di server
- Aplikasi meminta permission location saat dibutuhkan
- Semua komunikasi menggunakan HTTPS/SSL

## 🐛 Troubleshooting

### Peta Tidak Muncul
```
✓ Pastikan API Key sudah dikonfigurasi dengan benar
✓ Cek koneksi internet
✓ Verifikasi Google Maps API sudah aktif di Google Cloud Console
```

### Bus Tidak Terlihat
```
✓ Pastikan ada bus yang sedang aktif/beroperasi
✓ Cek koneksi ke backend server
✓ Reload aplikasi (swipe down to refresh)
```

### ETA Tidak Akurat
```
✓ Pastikan GPS device aktif
✓ Berikan permission lokasi ke aplikasi
✓ Pastikan berada di area outdoor untuk GPS lebih akurat
```

## 🤝 Kontribusi

Kontribusi sangat diterima! Silakan ikuti langkah berikut:

1. Fork repository ini
2. Buat branch baru (`git checkout -b feature/AmazingFeature`)
3. Commit perubahan (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

## 📝 Roadmap

- [ ] Notifikasi push untuk bus favorit
- [ ] Bookmark rute/trayek favorit
- [ ] History perjalanan
- [ ] Integrasi payment untuk pembelian tiket
- [ ] Dark mode
- [ ] Multi bahasa (EN/ID)
- [ ] Offline mode dengan cache data
- [ ] Voice assistant untuk pencarian

## 👥 Tim Pengembang

- **Your Name** - *Lead Developer* - [GitHub](https://github.com/yourusername)
- **Team Member** - *Backend Developer*
- **Team Member** - *UI/UX Designer*

## 📄 Lisensi

Project ini dilisensikan dibawah MIT License - lihat file [LICENSE](LICENSE) untuk detail.

## 📞 Kontak & Support

- **Email**: support@bustracker.com
- **Website**: [www.bustracker.com](https://www.bustracker.com)
- **Issue Tracker**: [GitHub Issues](https://github.com/username/bus-tracker-app/issues)

## 🙏 Acknowledgments

- Google Maps Platform untuk maps API
- Firebase untuk real-time database
- Material Design untuk UI components
- Komunitas Android Developer Indonesia

---

<div align="center">
  <p>Dibuat dengan ❤️ untuk memudahkan perjalanan masyarakat</p>
  <p>⭐ Jangan lupa berikan star jika project ini membantu! ⭐</p>
</div>
