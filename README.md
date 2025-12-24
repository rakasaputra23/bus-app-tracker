# 🚌 Bus Tracker - Aplikasi Pelacakan Armada Bus Real-Time

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-21-blue.svg)](https://developer.android.com/about/versions/lollipop)
[![License](https://img.shields.io/badge/License-MIT-red.svg)](LICENSE)

> Aplikasi Android untuk penumpang yang memungkinkan pelacakan posisi armada bus secara real-time, pencarian rute, informasi okupansi, dan estimasi waktu kedatangan.

## 📱 Tentang Aplikasi

**Bus Tracker** adalah modul aplikasi mobile berbasis Android Native (Java) yang dirancang khusus untuk calon penumpang dan masyarakat umum. Aplikasi ini menyediakan informasi real-time mengenai keberadaan armada bus, memudahkan pengguna dalam merencanakan perjalanan mereka dengan lebih efisien.

---

## ✨ Fitur Utama

### 1. 🗺️ Peta Sebaran Armada (All-Bus View)

- **Tampilan Otomatis** - Saat aplikasi dibuka, pengguna langsung disajikan peta digital yang menampilkan seluruh armada bus yang sedang beroperasi secara bersamaan
- **Real-Time Tracking** - Posisi bus-bus diperbarui secara real-time dan dapat dilihat bergerak di peta
- **Tanpa Pencarian Awal** - Pengguna dapat melihat ketersediaan bus tanpa harus melakukan pencarian terlebih dahulu
- **Interface Interaktif** - Peta yang mudah digunakan dengan fitur zoom dan navigasi

### 2. 🔍 Pencarian dan Filter (Search Engine)

Fitur pencarian canggih untuk memfilter tampilan bus di peta berdasarkan:

| Kriteria | Deskripsi | Contoh |
|----------|-----------|--------|
| **Jurusan/Trayek** | Filter berdasarkan rute perjalanan | "Jakarta - Bandung" |
| **Kelas Bus** | Filter berdasarkan kategori layanan | Ekonomi / Eksekutif |
| **Plat Nomor** | Pencarian bus spesifik | B 1234 XYZ |

### 3. ℹ️ Detail Info & Okupansi

Ketika pengguna menekan salah satu ikon bus di peta, akan ditampilkan informasi lengkap:

- ✓ **Posisi Saat Ini** - Lokasi bus secara real-time
- ✓ **Rute** - Trayek perjalanan bus
- ✓ **Sisa Kursi** - Jumlah kursi yang masih tersedia
- ✓ **Jumlah Penumpang** - Informasi okupansi real-time

### 4. ⏱️ Estimasi Kedatangan (ETA)

- Menampilkan perkiraan waktu bus akan tiba di lokasi pengguna
- Kalkulasi otomatis berbasis GPS dan kondisi lalu lintas
- Update dinamis sesuai pergerakan bus
- Akurasi tinggi untuk perencanaan perjalanan

---

## 🛠️ Teknologi yang Digunakan

<table>
<tr>
<td width="50%">

### Core Technologies
- **Platform**: Android Native
- **Bahasa**: Java
- **IDE**: Android Studio
- **Min SDK**: API 21 (Android 5.0 Lollipop)
- **Target SDK**: API 34 (Android 14)

</td>
<td width="50%">

### Libraries & Services
- **Maps**: Google Maps Android API
- **Real-time**: Firebase Realtime Database
- **Location**: Google Play Services Location
- **Networking**: Retrofit 2 + OkHttp
- **JSON Parsing**: Gson

</td>
</tr>
</table>

### Architecture & Design Pattern

```
├── MVVM (Model-View-ViewModel)
├── Repository Pattern
├── Dependency Injection
└── Clean Architecture
```

---

<div align="center">

**📦 Version 1.0.0** | **👥 Modul Android Penumpang (Publik)**

*Dibuat dengan ❤️ untuk memudahkan perjalanan masyarakat*

</div>
