# Sistem Pemesanan Kantin Cepat

Proyek Tugas Besar (Tubes) ini adalah sistem pemesanan kantin client-server menggunakan Java Swing, Java Sockets, dan database PostgreSQL.

**Teknologi:**

- **Java 17**
- **Java Swing** (untuk GUI)
- **PostgreSQL 17** (untuk Database)
- **Maven** (untuk Build & Dependency)

---

## 🚀 Alur Kerja Git (PENTING!)

Kita menggunakan alur kerja `develop` -> `main`.

1.  **`main`**: Branch ini **HANYA** untuk versi yang sudah stabil dan siap demo. **DILARANG PUSH LANGSUNG KE MAIN.**
2.  **`developer`**: Ini adalah _branch_ utama kita untuk bekerja. Semua fitur akan di-_merge_ ke sini.
3.  **Branch Fitur**: Saat mengerjakan tugas, **JANGAN** _ngoding_ di `developer`. Buat _branch_ baru dari `developer`.
    - Contoh: `git checkout developer`
    - Contoh: `git checkout -b feat/login-customer` (jika mengerjakan fitur login)
4.  **Pull Request (PR)**: Setelah fitur Anda selesai, buat _Pull Request_ dari `branch-fitur-anda` ke `developer`. Setelah di-review dan di-tes oleh anggota lain, baru akan di-_merge_.

---

## 👨‍💻 Panduan Setup untuk Developer

Ikuti langkah ini agar bisa langsung mulai _ngoding_.

### 1. Prasyarat (Wajib Install)

- **Java JDK 17**: Pastikan versi Java Anda 17.
- **Git**: Untuk _version control_.
- **PostgreSQL 17**: Database server. Pastikan Anda menginstalnya dari [EDB](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) dan sudah membuat _user_ & _password_.
- **IDE**: VS Code (dengan Java Extension Pack) atau IntelliJ IDEA.
- **Maven**: Biasanya sudah terinstal bersama IDE.

### 2. Clone & Setup Proyek

```bash
# 1. Clone repository ini
git clone https://github.com/darafinaselen/kantin-cepat
cd kantin-cepat

# 2. Pindah ke branch kerja kita (jika Anda clone baru)
git checkout developer
```

### 3. Setup Database (Lokal)

1. Buka pgAdmin (atau tool DB Anda).
2. Buat database baru. WAJIB beri nama: kantin_cepat_db

### 4. Setup File Konfigurasi (Lokal)

1. Buka proyek ini di IDE Anda.
2. Cari folder server/src/main/resources/.
3. Anda akan melihat file config.properties.example.
4. Buat duplikat file tersebut di tempat yang sama dan beri nama config.properties (File ini sudah ada di .gitignore jadi tidak akan ter-upload).
5. Buka config.properties dan edit isinya agar sesuai dengan user dan password PostgreSQL lokal Anda:

# Ganti 'password_anda' dengan password Postgres Anda

```bash
DB_URL=jdbc:postgresql://localhost:5432/kantin_cepat_db
DB_USER=postgres
DB_PASS=password_anda

SERVER_PORT=8080
```

### 5. Build Proyek

IDE Anda (IntelliJ/VS Code) akan otomatis mendeteksi ini sebagai proyek Maven. Izinkan dia men-download semua dependency (seperti driver PostgreSQL).

Anda siap untuk ngoding!

📖 Pembagian Tugas
**Developer A (Backend Core & DB)**

- Bertanggung jawab atas modul server.
- Membuat ServerMain.java, ClientHandler.java, dan koneksi JDBC.
- Mendefinisikan protokol komunikasi (misal: "LOGIN|user|pass").
- File Utama: server/

**Developer B (Admin & CRUD)**

- Bertanggung jawab atas GUI Admin (CRUD Menu, CRUD User).
- File Utama: client/src/main/java/.../view/AdminDashboardView.java
- File Pendukung: common/ (untuk Menu.java)

**Developer C (Customer & Order)**

- Bertanggung jawab atas GUI Pelanggan (Lihat menu, keranjang, checkout, tracking pesanan).
- File Utama: client/src/main/java/.../view/CustomerDashboardView.java
- File Pendukung: common/ (untuk Pesanan.java)

**Developer D (Dapur & Fitur Real-time)**

- Bertanggung jawab atas GUI Dapur (Melihat antrian pesanan, update status).
- Membantu implementasi Live Chat (GUI dan logika relay di server).
- File Utama: client/src/main/java/.../view/KitchenDashboardView.java
