-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Insert sample users (passwords are stored in plain text - vulnerability!)
INSERT INTO users (username, password, email, role) VALUES 
    ('admin', 'admin123', 'admin@example.com', 'ADMIN'),
    ('john', 'password123', 'john@example.com', 'USER'),
    ('jane', 'jane2023', 'jane@example.com', 'USER'),
    ('bob', 'bob456', 'bob@example.com', 'USER'),
    ('alice', 'alice789', 'alice@example.com', 'MANAGER');

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL
);

-- Insert sample products
INSERT INTO products (name, description, price, stock) VALUES 
    ('Laptop', 'High-performance laptop', 999.99, 50),
    ('Mouse', 'Wireless mouse', 29.99, 200),
    ('Keyboard', 'Mechanical keyboard', 79.99, 150),
    ('Monitor', '27-inch 4K monitor', 399.99, 75),
    ('Headphones', 'Noise-cancelling headphones', 199.99, 100);

-- Made with Bob
