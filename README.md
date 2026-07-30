# SmartBill

SmartBill is a modern, responsive, and robust full-stack web application designed for electronics and smartphone retailers. It handles Shop Profile management, Product Inventory tracking, and professional GST Invoice PDF generation natively from the browser.

## Tech Stack

### Frontend
- **React 18** with **TypeScript**
- **Vite** for blazing fast builds
- **Tailwind CSS** for beautiful, responsive styling
- **React Router** for protected routing
- **React Hook Form** + **Zod** for robust client-side validation
- **Axios** for API communication
- **Lucide React** for modern iconography
- **React Hot Toast** for elegant notifications

### Backend
- **Java 21** & **Spring Boot 3.4.x**
- **Spring Security 6** with **JWT** Authentication (Stateless)
- **Spring Data JPA** & **Hibernate**
- **PostgreSQL** Database
- **OpenPDF** for native backend PDF generation

## Core Features
1. **Authentication:** Secure JWT-based admin login.
2. **Shop Profile:** Store Shop Name, Logo, Address, and GSTIN.
3. **Inventory Management:** Full CRUD operations for products with low-stock badges.
4. **Invoice Generation:** 
   - Dynamic billing form with real-time math (Subtotal, GST, Grand Total).
   - Validates available stock before billing.
   - Automatically deducts stock upon successful invoice generation.
5. **Invoice History:**
   - Search by Invoice Number or Customer Name.
   - Native UI "View Details" modal for quick previews.
   - Download or Print A4-sized GST Invoice PDFs seamlessly.

## Prerequisites
- Java 21+
- Node.js 20+
- PostgreSQL 15+

## Database Setup
Ensure PostgreSQL is running locally on port `5432`.
1. Create a database named `smartbill`.
2. Ensure the default Postgres user exists with the password `postgres`.
   - Alternatively, configure custom credentials via environment variables (see below).
3. The application uses `hibernate.ddl-auto=update` and will automatically generate all necessary tables (`shop`, `products`, `invoices`, `invoice_items`) on startup.

## Environment Variables
The application uses default fallbacks if environment variables are missing, making it easy to start immediately. 

**Backend** (`smartbill-backend`):
Can be set via `.env` or system variables:
- `DB_HOST` (default: localhost)
- `DB_PORT` (default: 5432)
- `DB_NAME` (default: smartbill)
- `DB_USER` (default: postgres)
- `DB_PASSWORD` (default: postgres)
- `JWT_SECRET` (default: 256-bit strong base64 secret)

**Frontend** (`smartbill-frontend`):
Configure in `smartbill-frontend/.env`:
- `VITE_API_URL` (default: http://localhost:8080/api/v1)

## Installation & Running Locally

### Backend (Spring Boot)
1. Open a terminal in the `smartbill-backend` directory.
2. Compile and run:
```bash
./mvnw clean compile
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080`.

### Frontend (React/Vite)
1. Open a terminal in the `smartbill-frontend` directory.
2. Install dependencies:
```bash
npm install
```
3. Start the development server:
```bash
npm run dev
```
The application will be accessible at `http://localhost:5173`.

### Initial Login
Since registration is out of scope for this admin portal, use the hardcoded admin credentials provided by the authentication service:
- **Email:** `admin@smartbill.com`
- **Password:** `password123`

## Architecture Highlights
- **SOLID Principles:** The backend strictly separates concerns across Controllers, Services, and Repositories.
- **DTO Pattern:** Entities are never exposed directly to the frontend. All data passes through Mapper layers (e.g. `InvoiceMapper`) into robust Data Transfer Objects.
- **Transactional Integrity:** Complex operations (like billing an invoice and deducting stock) occur inside `@Transactional` blocks to prevent database corruption.
- **Secure by Default:** Passwords are encrypted with BCrypt, and all routes are protected by stateless JWT filters.
