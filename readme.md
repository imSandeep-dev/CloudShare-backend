# CloudShare - Backend

CloudShare is a cloud-based file storage and sharing platform. This repository contains the backend service, built with Spring Boot, handling authentication, file storage, and payment processing.

## Features

- **Secure Authentication** — JWT-based auth integrated with Clerk for user identity management
- **Cloud File Storage** — Upload, store, and manage files using Cloudinary as the storage backend
- **Payment Integration** — Razorpay integration for handling subscription/premium plan payments
- **RESTful API** — Clean, resource-based endpoints for frontend consumption
- **Production Ready** — Deployed and running on Railway with tuned JVM memory configuration

## Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Security:** Spring Security + JWT
- **Authentication Provider:** Clerk
- **File Storage:** Cloudinary
- **Payments:** Razorpay
- **Database:** MongoDB
- **Deployment:** Railway (backend), Netlify (frontend)

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MongoDB instance (local or cloud, e.g. MongoDB Atlas)
- Cloudinary account and API credentials
- Razorpay account and API keys
- Clerk account and API keys

### Configuration

This project uses **profile-based configuration** for environment variables. Create an `application-local.properties` file (not committed to version control) inside `src/main/resources/`:

```properties
# MongoDB
spring.data.mongodb.uri=your_mongodb_connection_string

# Clerk
clerk.api.key=your_clerk_api_key

# Cloudinary
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_cloudinary_api_key
cloudinary.api-secret=your_cloudinary_api_secret

# Razorpay
razorpay.key-id=your_razorpay_key_id
razorpay.key-secret=your_razorpay_key_secret

# JWT
jwt.secret=your_jwt_secret
```

Run the application with the `local` profile active:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Build & Run

```bash
# Clone the repository
git clone https://github.com/imSandeep-dev/CloudShare-backend.git
cd cloudshare-backend

# Build
mvn clean install

# Run
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The server will start on `http://localhost:8080` by default.

## Deployment

The backend is deployed on **Railway**. When deploying, note:

- Environment variables should be set directly in the Railway dashboard rather than relying on a `.env` file (avoids compatibility issues with `spring-dotenv` on newer Spring Boot versions)
- If the deployment runs into OOM (Out of Memory) crashes, JVM heap size can be tuned via the `JAVA_TOOL_OPTIONS` environment variable, e.g.: