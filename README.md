[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/_uV8Mn8f)
# Invoice Management System (IMS) CLI

![Build](https://github.com/ithsjava25/project-jpa-grupp-3-d/actions/workflows/ci.yml/badge.svg)
![Open Pull Requests](https://img.shields.io/github/issues-pr-raw/ithsjava25/project-jpa-grupp-3-d)
![Closed Pull Requests](https://img.shields.io/github/issues-pr-closed-raw/ithsjava25/project-jpa-grupp-3-d)

## 📋 Overview
A CLI-based Invoice Management System for small businesses. Supports user authentication, company and client management, invoice processing, and company user administration.

---

## ✨ Features

**👤 User Management**
- Register and login with secure password hashing (BCrypt)
- Masked email logging
- Account deletion with cascade cleanup

**🏢 Company Management**
- Create companies with unique organization numbers
- Multi-user associations
- Manage company info (address, contact details)

**👥 Client Management**
- CRUD operations for clients
- Company-scoped clients
- Store name, address, email, and phone

**📄 Invoice Management**
- Full lifecycle: `CREATED → SENT → PAID → OVERDUE → CANCELLED`
- Add items with quantity and pricing
- Automatic total calculation
- Client-specific invoice tracking

**🔐 Validation & Security**
- Email format validation
- Business rule enforcement
- Entity existence validation
- SQL injection prevention via JPA

---

## 🏗️ Architecture

**Domain Model:**
User ↔ CompanyUser ↔ Company ↔ Client
↳ Invoice ↔ InvoiceItem


**Entities:**
- **User:** System credentials
- **Company:** Business entity with unique org number
- **CompanyUser:** Many-to-many relationships
- **Client:** Company-associated customer
- **Invoice:** Financial document
- **InvoiceItem:** Line items

**Design Patterns:** Repository, Service layer, DTO, Constructor DI

---

## 🚀 Getting Started

**Prerequisites:**
- Java 17+
- Maven 3.6+
- H2 Database (embedded)

**Installation:**
```bash
git clone <repository-url>
cd invoice-management-system
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.App"


🎮 Usage Guide

1. Authentication
Register or login
Password validation ≥8 chars
Email format check

2. Company Setup
Create new company (auto-associate creator)
Select existing company
Manage company information

3. Main Operations
Client Management: CRUD clients
Invoice Management: Create invoices, add items, update status
Company Users: Invite/remove users
Company Settings: Update info

4. Invoice Workflow
Select client
Enter invoice number & due date
Add items
Review totals & save


📁 Project Structure
src/main/java/org/example/
├── auth/        # Authentication services
├── entity/      # Domain entities & DTOs
│   ├── user/
│   ├── company/
│   ├── client/
│   └── invoice/
├── repository/  # Data access
├── service/     # Business logic
├── exception/   # Custom exceptions
└── util/        # Utilities

🧪 Testing
mvn test

Unit & integration tests
Business rule validation
Exception handling


🔧 Configuration

Database: Embedded H2, auto-generated schema, UUID PKs, timestamp auditing
Logging: SLF4J with debug/info, masked emails, transaction logs


💾 Data Persistence

JPA/Hibernate ORM
Cascade operations & optimistic locking
Auditing fields: createdAt, updatedAt


🚫 Error Handling

Custom exception hierarchy
User-friendly messages
Transaction rollback on failure


📊 Sample Operations

Invoice:

Select client
Enter invoice number & due date
Add line items
Save & review total


Company Users:

List users
Invite by email
Remove user (cannot remove self)


Invoice Status Flow:

CREATED → SENT → PAID
         ↘ OVERDUE
         ↘ CANCELLED



Business Rules:

Unique company org numbers
Unique invoice numbers per company
Users cannot remove themselves
Clients belong to a single company
Invoice requires ≥1 item


🙏 Acknowledgments
Spring Security (password encoding)
Lombok (reduce boilerplate)
Hibernate ORM
JUnit & Mockito
CLI designed for learning & small businesses.
