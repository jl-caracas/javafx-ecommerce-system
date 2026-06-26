<<<<<<< HEAD
# 🛒 CØDE LØCK E-Commerce App

A full-featured **JavaFX Desktop E-Commerce Application** built with **Maven**, **SQLite**, and **JasperReports**. Originally developed as an OOP project, it now includes a modern admin dashboard, product variant system, bcrypt password security, and PDF receipt generation.

---

## ✨ Features

### 👤 Customer Features
- **Product Catalog** — Browse, search by name, and filter by category
- **Product Variants** — Select specifications (size/color) and see real-time price adjustments
- **Shopping Cart** — Add/remove items, adjust quantities, select which items to checkout
- **User Accounts** — Register, login/logout, cart persists across sessions
- **Order Receipts** — PDF receipt generated automatically after checkout

### 🔧 Admin Features
- **Admin Panel** — Full CRUD for products (with image upload) and product variants
- **Order Management** — View pending/completed orders, mark orders as completed
- **Dashboard** — Modern analytics: product count, pending/completed orders, total revenue, pie chart
- **Receipt Viewing** — Generate and open PDF receipts for any order

### 🛡️ Security
- **bcrypt Password Hashing** — Passwords are hashed before storage; legacy plaintext passwords auto-upgraded on login
- **Role-Based UI** — Orders & Dashboard buttons hidden from regular customers

---

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/com/ecommerce/
│   │   ├── model/          # POJO classes (Product, User, Order, CartItem, etc.)
│   │   ├── db/             # Data Access Objects + DatabaseManager
│   │   ├── ui/             # JavaFX UI windows (9 screens)
│   │   ├── report/         # JasperReports PDF receipt generator
│   │   └── utils/          # Test utilities
│   └── resources/
│       ├── css/style.css   # Application stylesheet
│       ├── images/         # Product images
│       └── reports/        # JasperReports JRXML template
└── test/                   # Test resources
```

### 📁 Package Breakdown

| Package | Files | Purpose |
|---|---|---|
| `com.ecommerce.model` | 7 | Product, ProductVariant, CartItem, ShoppingCart, Order, OrderItem, User |
| `com.ecommerce.db` | 6 | DatabaseManager, DatabaseInitializer, ProductDAO, ProductVariantDAO, CartDAO, OrderDAO, UserDAO |
| `com.ecommerce.ui` | 9 | MainApp, LandingWindow, LoginWindow, RegisterWindow, CartWindow, VariantSelectionWindow, AdminPanelWindow, OrderHistoryWindow, DashboardWindow |
| `com.ecommerce.report` | 1 | ReceiptGenerator (JasperReports XML-based PDF) |
| `com.ecommerce.utils` | 1 | TestDatabase |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- JavaFX SDK 17+ (resolved automatically by Maven plugin)

### Installation & Running

```bash
# 1. Clone or navigate to the project
cd EcommerceAppMaven

# 2. Compile the project
mvn clean compile

# 3. Run the application (uses JavaFX Maven Plugin)
mvn javafx:run
```

The application will:
1. Create an SQLite database (`ecommerce.db`) in the project root
2. Insert 10 sample products on first run
3. Create a default admin account

### Default Admin Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |

### Entry Points

| Class | Description |
|---|---|
| `LandingWindow` | Landing screen with "SHOP NOW" button **(main entry point)** |
| `MainApp` | Product catalog after clicking SHOP NOW |
| `AdminPanelWindow` | Admin panel (accessible via 🔧 button when logged in as admin) |

---

## 🖥️ Screens Overview

| Screen | File | Description |
|---|---|---|
| **Landing** | `LandingWindow.java` | Welcome page with SHOP NOW button |
| **Catalog** | `MainApp.java` | Product grid with search, category filter, cart badge |
| **Login** | `LoginWindow.java` | Modal login with Enter-key support |
| **Register** | `RegisterWindow.java` | Account creation with validation |
| **Cart** | `CartWindow.java` | Item selection, quantity adjustment, checkout |
| **Variant Select** | `VariantSelectionWindow.java` | Choose product spec/color with pricing |
| **Admin Panel** | `AdminPanelWindow.java` | Product CRUD, order management tabs |
| **Order History** | `OrderHistoryWindow.java` | View past orders and generate receipts |
| **Dashboard** | `DashboardWindow.java` | Analytics with stat cards and pie chart |

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 17 | Language |
| **JavaFX** | 17.0.13 | Desktop GUI framework |
| **Maven** | 3.11+ | Build & dependency management |
| **SQLite** | 3.53.1.0 | Embedded database (via JDBC) |
| **JasperReports** | 6.21.3 | PDF receipt generation |
| **OpenPDF** | 1.3.30 | PDF export library |
| **jBCrypt** | 0.4 | Password hashing |
| **ECJ (Eclipse Compiler)** | 3.21.0 | JRXML compilation |

---

## 📄 Database Schema (SQLite)

```
products         (id, name, description, category, price, stock, image_path)
product_variants (id, product_id, spec_name, color, price_modifier, stock)
users            (id, username, password, full_name, address, gcash_number, role)
orders           (id, customer_name, user_id, order_date, total, status)
order_items      (id, order_id, product_name, product_price, quantity)
user_cart        (user_id, variant_id, quantity)
```

---

## 🔧 Recent Fixes & Improvements

- ✅ **bcrypt password hashing** — passwords securely stored and verified
- ✅ **Legacy password upgrade** — plaintext passwords auto-upgraded on login
- ✅ **Cart data preserved** — `user_cart` table no longer dropped on startup
- ✅ **User-specific order history** — customers see only their own orders
- ✅ **ProductDAO returns generated ID** — no more fragile "search by name" workaround
- ✅ **Admin orders table** — columns properly sized with `UNCONSTRAINED_RESIZE_POLICY`
- ✅ **Modern Dashboard** — stat cards with hover effects, styled pie chart
- ✅ **Role-based UI** — Orders & Dashboard hidden from customers, shown for admins
- ✅ **Variant table styling** — transparent rows with orange column headers

---

## 📝 License

This project was created as a college OOP course project. Free to use and modify for educational purposes.
=======
# javafx-ecommerce-system
A JavaFX desktop e-commerce app built with Maven, SQLite, and JasperReports. Features product catalog with variant selection, shopping cart, admin panel for product &amp; order management, analytics dashboard, bcrypt password security, and automated PDF receipt generation.
>>>>>>> 23ff5d17c78ee573e1f68ef6333d903c2ec1097c
