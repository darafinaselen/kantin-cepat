DROP TABLE IF EXISTS live_chat;
DROP TABLE IF EXISTS order_details;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS order_status;

-- ====== MEMBUAT TIPE DATA KUSTOM (ENUM) ======

-- Tipe ENUM untuk peran pengguna
CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'DAPUR',
    'PELANGGAN'
);

-- Tipe ENUM untuk status pesanan
CREATE TYPE order_status AS ENUM (
    'DITERIMA',
    'DIMASAK',
    'SIAP_DIAMBIL',
    'SELESAI',
    'DIBATALKAN'
);

-- ====== MEMBUAT TABEL-TABEL ======

-- Tabel untuk Kategori Menu (Makanan, Minuman, dll)
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Tabel untuk Pengguna (Admin, Dapur, Pelanggan)
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Di proyek nyata, ini harus di-hash
    role user_role NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabel untuk Menu Makanan/Minuman
CREATE TABLE menu (
    menu_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price INT NOT NULL,
    category_id INT NOT NULL,
    image_path VARCHAR(255), -- Path ke file gambar
    is_available BOOLEAN DEFAULT TRUE,
    estimated_time_minutes INT,
    
    CONSTRAINT fk_category
        FOREIGN KEY(category_id) 
        REFERENCES categories(category_id)
        ON DELETE SET NULL -- Jika kategori dihapus, menu tetap ada tapi kategorinya null
);

-- Tabel "Kepala" Pesanan
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    total_price INT NOT NULL,
    status order_status NOT NULL DEFAULT 'DITERIMA',
    order_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_customer
        FOREIGN KEY(customer_id) 
        REFERENCES users(user_id)
);

-- Tabel Detail Pesanan (Penghubung Many-to-Many antara orders dan menu)
CREATE TABLE order_details (
    order_detail_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    menu_id INT NOT NULL,
    quantity INT NOT NULL,
    price_at_order INT NOT NULL, -- Menyimpan harga saat pesan (jika harga menu berubah)
    
    CONSTRAINT fk_order
        FOREIGN KEY(order_id) 
        REFERENCES orders(order_id)
        ON DELETE CASCADE, -- Jika pesanan dihapus, detailnya ikut terhapus
    CONSTRAINT fk_menu
        FOREIGN KEY(menu_id) 
        REFERENCES menu(menu_id)
        ON DELETE RESTRICT -- Jangan biarkan menu dihapus jika masih ada di pesanan
);

-- Tabel untuk Live Chat
CREATE TABLE live_chat (
    chat_id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message_content TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_sender
        FOREIGN KEY(sender_id) 
        REFERENCES users(user_id),
    CONSTRAINT fk_receiver
        FOREIGN KEY(receiver_id) 
        REFERENCES users(user_id)
);

-- ====== MEMASUKKAN DATA DUMMY (CONTOH) ======

-- 1. Isi Kategori
INSERT INTO categories (name) VALUES ('Makanan Utama'), ('Minuman'), ('Cemilan');

-- 2. Isi Pengguna
-- (Password "admin123", "dapur123", "pelanggan123". Nanti ini harus di-hash!)
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'ADMIN'),
('dapur', 'dapur123', 'DAPUR'),
('pelanggan', 'pelanggan123', 'PELANGGAN');

-- 3. Isi Menu
INSERT INTO menu (name, description, price, category_id, image_path, is_available, estimated_time_minutes)
VALUES 
('Nasi Goreng Spesial', 'Nasi goreng dengan telur, ayam, dan bakso', 25000, 1, 'assets/nasi_goreng.jpg', TRUE, 15),
('Es Teh Manis', 'Teh segar dengan es dan gula', 5000, 2, 'assets/es_teh.jpg', TRUE, 3),
('Kentang Goreng', 'Kentang goreng renyah dengan saus', 15000, 3, 'assets/kentang_goreng.jpg', TRUE, 10),
('Ayam Geprek', 'Ayam goreng pedas dengan sambal bawang', 18000, 1, 'assets/ayam_geprek.jpg', FALSE, 20); -- Contoh item tidak tersedia

COMMIT;