# 📝 ToDo List App – Fullstack (Spring Boot + React + Docker)

A full-featured ToDo application built with:

- ✅ Java 17 + Spring Boot 3.5
- ✅ MySQL 8 (Dockerized)
- ✅ React + Vite + TypeScript
- ✅ JWT Authentication (Access + Refresh)
- ✅ Role-based access (USER / ADMIN)
- ✅ Full Docker support via `docker-compose`

---

## 👨‍💻 About the Project

This is a personal project developed by me, a backend-focused developer (Java + Spring Boot), to demonstrate:

- Clean architecture and layered backend
- RESTful API design with proper DTOs
- Secure JWT authentication
- Role-based access: promoting, deleting, and managing users as ADMIN
- Full Docker setup with backend, frontend, and MySQL

> ⚠️ **Frontend implementation was assisted by external support**. My focus was backend development.

---

## 🛠️ Tech Stack

| Layer     | Technology                 |
|-----------|----------------------------|
| Backend   | Java 17, Spring Boot, JPA  |
| Frontend  | React, Vite, TypeScript    |
| Database  | MySQL                      |
| Auth      | Spring Security + JWT      |
| Deploy    | Docker, Docker Compose     |

---

## 🧪 Features

- Register / Login / Logout
- JWT with Refresh Tokens
- Users can manage their own tasks (CRUD)
- Admins can:
  - View all users
  - Promote other users to admin
  - Delete users (with token cascade)
  - View task statistics per user

---

## 🚀 How to Run (Locally)

Make sure you have Docker + Docker Compose.

```bash
docker-compose up --build
