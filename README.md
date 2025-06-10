# Aplikasi Pemesanan Makanan & Minuman

Aplikasi Android ini adalah simulasi sederhana untuk pemesanan makanan dan minuman dengan fokus pada antarmuka pengguna yang modern dan fitur inti yang biasa ada di aplikasi sejenis.

## Daftar Isi
- [Deskripsi Aplikasi](#deskripsi-aplikasi)
- [Cara Penggunaan](#cara-penggunaan)
- [Implementasi Teknis Singkat](#implementasi-teknis-singkat)
- [Persyaratan & Konfigurasi](#persyaratan--konfigurasi)

## Deskripsi Aplikasi
Aplikasi ini memungkinkan pengguna untuk:
- Melakukan pendaftaran akun dan login sederhana.
- Menjelajahi daftar menu makanan dan minuman.
- Mencari menu berdasarkan kata kunci.
- Memfilter menu berdasarkan kategori.
- Menambahkan item ke keranjang dan mengatur kuantitas.
- Melihat ringkasan pesanan dan mensimulasikan checkout.
- Mengirim data pesanan ke Google Sheets.
- Mengubah tema aplikasi (terang/gelap).
- Mengakses data offline jika koneksi internet terputus.

## Cara Penggunaan

1.  **Luncurkan Aplikasi:** Aplikasi akan dimulai dengan *splash screen*.
2.  **Login/Daftar:** Jika ini pertama kali, Anda akan diarahkan ke halaman daftar. Buat akun baru, lalu login.
3.  **Jelajahi Menu:** Di halaman utama, Anda dapat melihat daftar menu. Gunakan kategori di bagian atas atau bilah pencarian untuk menemukan item spesifik.
4.  **Tambah ke Keranjang:** Klik item menu untuk melihat detailnya, lalu tambahkan ke keranjang dengan kuantitas yang diinginkan.
5.  **Ringkasan Pesanan & Checkout:** Pergi ke tab "Keranjang", lalu klik "Lanjutkan Pembayaran" untuk melihat ringkasan pesanan. Klik "PESAN SEKARANG" untuk mensimulasikan pembayaran dan mencatat pesanan Anda ke Google Sheets.
6.  **Pengaturan Tema:** Di tab "Settings", Anda dapat beralih antara tema terang dan gelap.
7.  **Logout:** Tombol "Logout" juga tersedia di tab "Settings".

## Implementasi Teknis Singkat

Aplikasi ini dibangun menggunakan **Android Studio** dengan bahasa pemrograman **Java**.

* **Arsitektur:** Menggunakan `Activity` sebagai host utama dan `Fragment` untuk bagian-bagian UI yang berbeda, diatur menggunakan `Navigation Component`.
* **Pengambilan Data:** Data menu diambil dari [Spoonacular API](https://spoonacular.com/food-api/docs) menggunakan library **Retrofit** dan **Gson**. Paginasi (infinite scrolling) diimplementasikan untuk memuat data secara bertahap.
* **Autentikasi:** Sistem login dan pendaftaran sederhana menggunakan **SharedPreferences** untuk menyimpan kredensial akun secara lokal.
* **Keranjang Belanja:** Data keranjang dikelola oleh kelas `CartManager` (singleton) dan disimpan secara persisten menggunakan **SharedPreferences**.
* **Integrasi Google Sheets:** Data pesanan dikirim ke Google Sheets melalui API Web yang dibangun dengan **Google Apps Script[Link](https://docs.google.com/spreadsheets/d/1kGki62B4qx8pBvt3hIDgoccXTwc09ksiRgpZpW-pm1c/edit?usp=sharing) .
* **Tema:** Aplikasi mendukung tema terang dan gelap, preferensi disimpan di **SharedPreferences** dan diterapkan saat *Activity* diluncurkan.
* **Penanganan Offline:** Aplikasi dapat menampilkan data yang di-*cache* dari `SharedPreferences` sebagai *fallback* jika tidak ada koneksi internet atau panggilan API gagal.

## Persyaratan & Konfigurasi

* **Android Studio** dan **JDK**.
* **API Key Spoonacular:** Diperlukan untuk mengambil data menu. Pastikan Anda memasukkan API Key Anda di `HomeFragment.java`.
* **Google Sheet & Google Apps Script:** Diperlukan untuk fitur pencatatan pesanan. Anda harus mendeploy Google Apps Script sebagai aplikasi web dan memasukkan URL yang benar di `OrderSummaryFragment.java`.

---
