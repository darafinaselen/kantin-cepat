-- RESET (Hapus semua urut dari bawah ke atas karena foreign key)
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS order_details;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS users;
DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS menu_category;
DROP TYPE IF EXISTS order_status;
DROP TABLE IF EXISTS live_chat;

DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS categories;

-- 1. ENUM TYPES
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'ADMIN', 'KITCHEN');
CREATE TYPE menu_category AS ENUM ('MEALS', 'DRINK', 'SNACK');
CREATE TYPE order_status AS ENUM ('PENDING', 'COOKING', 'READY', 'COMPLETED', 'CANCELLED');

-- 2. TABLE USERS
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    role user_role DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABLE MENU
CREATE TABLE menu_items (
    menu_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category menu_category NOT NULL,
    image_path VARCHAR(255),
    is_available BOOLEAN DEFAULT TRUE
);

-- 4. TABLE ORDERS
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT,
    total_amount DECIMAL(10, 2),
    status order_status DEFAULT 'PENDING',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 5. TABLE ORDER DETAILS
CREATE TABLE order_details (
    detail_id SERIAL PRIMARY KEY,
    order_id INT,
    menu_id INT,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2),
    notes VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (menu_id) REFERENCES menu_items(menu_id)
);

-- 6. TABLE CHAT MESSAGES (BARU! ✨)
CREATE TABLE chat_messages (
    chat_id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL,   -- Siapa yang kirim
    receiver_id INT,          -- Siapa yang terima (Bisa NULL jika broadcast ke semua Admin)
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

-- SEEDING DATA
INSERT INTO users (username, email, password, full_name, phone_number, role) VALUES 
('admin', 'admin@kantin.com', 'admin123', 'Admin Kantin', '081234567890', 'ADMIN'),
('dapur', 'dapur@kantin.com', 'dapur123', 'Staff Dapur', '081234567891', 'KITCHEN'),
('user1', 'wahyuni@gmail.com', 'user123', 'Wahyunii Sulastri', '081992194938', 'CUSTOMER');

INSERT INTO menu_items (name, description, price, category, is_available) VALUES
('Ayam Geprek', 'Ayam geprek pedas nampol', 10000, 'MEALS', TRUE),
('Es Teh', 'Es teh manis segar', 5000, 'DRINK', TRUE);

-- Dummy Chat
INSERT INTO chat_messages (sender_id, receiver_id, message) VALUES
(3, 1, 'Halo min, ayam gepreknya masih ada?');