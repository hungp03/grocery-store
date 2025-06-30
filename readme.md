# Grocery Store E-commerce System

This is a simple e-commerce platform developed for learning purposes, with a focus on grocery items. Created as part of the Service Oriented Software Development course, the project introduces a basic  system that applies web technologies and service-oriented architecture (SOA) principles in a clear and accessible way.

## Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
  - [User Features (Client-Side)](#user-features-client-side)
  - [Administrator Features (Admin-Side)](#administrator-features-admin-side)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation & Running](#installation--running)
- [Docker Deployment](#docker-deployment)
- [Contributing](#contributing)
- [License](#license)

---

## About the Project

This project builds a complete e-commerce system for grocery items, following a service-oriented architecture (SOA) and communicating via RESTful APIs. 

## Features

### User Features (Client-Side)

- **Authentication:**  
  - Secure authentication with JWT.
  - Google OAuth external login supported.
  - Token revocation for enhanced security.
- **Browse & Search Products:**  
  - Filter and sort functionality.
- **Product Details:**  
  - Detailed product view.
- **Cart Management:**  
  - Add, remove, update quantity, and select products for ordering.
- **Order & Payment:**  
  - Integrated VN PAY Sandbox for online payment demo.
- **Personal Page:**  
  - Manage personal info.
  - View purchase history (with order cancellation for undelivered orders).
  - Manage favorite products.
  - View/manage logged-in devices.
  - Account management (disable, update password, forgot password).
  - Product reviews.
- **Upload Image:**
  - Upload image to Cloudinary

### Administrator Features (Admin-Side)

- **Dashboard:**  
  - Monthly revenue statistics and charts.
- **Product & Category Management**
- **Order Management**  
- **User Management:**  
  - Lock/Unlock user accounts.
- **Feedback Management:**  
  - Hide/Unhide product feedback.

## Architecture

- **Frontend:**  
  - User-facing web client for shopping and account management.
- **Backend:**  
  - RESTful API server handling business logic, authentication, and data operations.
- **Android App:**  
  - Native Android application for mobile shopping experience.
- **Dockerized:**  
  - Unified environment setup for development and production.

## Tech Stack

**Android App:**
- MVVM architecture
- Android (Java)
- Retrofit client for API communication
- Material-UI

**Web Client:**
- React (Vite)
- Tailwind CSS
- Ant Design (antd)
- Redux
- Axios

**Backend:**
- Spring Boot
- Maven
- Spring Security
- JPA (Java Persistence API)

**Cache:**
- Redis

**Database:**
- MySQL

**Deployment:**
- Docker
- Docker Compose

**Other Integrations:**
- Cloudinary (image hosting)
- VN PAY Sandbox (payment gateway integration)
- JWT & Google OAuth (authentication)

## Getting Started

### Prerequisites

- [Docker](https://www.docker.com/get-started)
- (Optional: Java, Node.js, npm, depending on local dev setup)

### Installation & Running

1. **Clone the repository:**
   ```sh
   git clone https://github.com/hungp03/grocery-store.git
   cd grocery-store
   ```

2. **Configure environment variables:**  
   - Fill in the required `.env` files for the server and client (see `/server/.env.example` and `/client/.env.example`).

3. **Start with Docker:**
   ```sh
   docker-compose up --build
   ```

4. **Access the application:**
   - Frontend: `http://localhost:3000/` (or specified port)
   - Backend API: `http://localhost:8080/` (or specified port)

## Docker Deployment

- The project includes `docker-compose.yml` for orchestrated deployment of frontend, backend, and supporting services.
- For production, adjust environment variables as needed.

---

**Author:** hungp03  
