# 🛒 Grocery Store Management System

A Java Swing-based desktop application that helps manage grocery store operations through separate Administrator and Customer modules. The system supports inventory management, billing, coupon discounts, shopping cart functionality, and file-based data persistence.

---

# Features

## Administrator Module

- Add Products
- Update Products
- Delete Products
- Manage Categories
- Manage Customers
- View Sales Reports

## Customer Module

- User Registration & Login
- Browse Products
- Search Products
- Add Items to Cart
- Apply Coupon Discounts
- Generate Bills
- Place Orders

---

# Interface Showcase

<table width="100%">
<tr>
<td width="50%" align="center">

**Login Screen**

<img src="images/login.png" width="100%"/>

</td>

<td width="50%" align="center">

**Admin Dashboard**

<img src="images/admin_dashboard.png" width="100%"/>

</td>
</tr>

<tr>
<td width="50%" align="center">

**Customer Dashboard**

<img src="images/customer_dashboard.png" width="100%"/>

</td>

<td width="50%" align="center">

**Billing Window**

<img src="images/billing.png" width="100%"/>

</td>
</tr>
</table>

---

# Key Features

- Role-Based Authentication
- Inventory Management
- Product Search
- Shopping Cart
- Coupon Discount System
- Billing System
- Order Management
- File-Based Data Storage
- Sales Report Generation
- Java Swing GUI

---

# Technologies Used

- Java
- Java Swing
- Java Collections Framework
- Object-Oriented Programming
- File Handling

---

# Architecture Overview

```
                User
                  │
                  ▼
          Java Swing Interface
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
  Admin Module       Customer Module
        │                   │
        └─────────┬─────────┘
                  ▼
          Business Logic Layer
                  │
                  ▼
           File Storage (.txt)
```

---

# Project Structure

```
Grocery_Store_Management_System
│
├── billing/
├── product/
├── storage/
├── ui/
├── user/
│
├── categories.txt
├── products.txt
├── users.txt
├── orders.txt
├── README.md
└── .gitignore
```

---

# Installation

## Prerequisites

- Java JDK 17 (or your project's required version)
- IntelliJ IDEA / Eclipse / VS Code

## Clone Repository

```bash
git clone https://github.com/bharathk572/Grocery_Store_Management_System.git
```

## Open Project

Open the project in your preferred Java IDE.

## Compile

Compile all Java source files.

## Run

Execute

```text
ui/MainFrame.java
```

---

# Data Storage

The application stores data using text files.

- users.txt
- products.txt
- categories.txt
- orders.txt

---

# Future Improvements

- Database Integration (MySQL)
- Barcode Scanner Support
- Invoice PDF Generation
- Online Payment Gateway
- Sales Analytics Dashboard
- Low Stock Notifications

---

# Author

**Bharath K**

GitHub:
https://github.com/bharathk572


---

# License

This project is developed for educational purposes.