# Krainet User Management System

A microservices-based user management system with authentication, authorization, and notification capabilities.

## System Architecture

The system consists of two main microservices:

1. **Auth Service**
   - Handles user authentication and authorization
   - Manages user accounts (CRUD operations)
   - Implements JWT-based security
   - Exposes RESTful APIs

2. **Notification Service**
   - Sends email notifications for user-related events
   - Listens to Kafka events from Auth Service
   - Handles email delivery

## Prerequisites

- Docker and Docker Compose
- Java 17+
- Maven

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd krainet-test
```

### 2. Environment Setup

Create a `.env` file in the root directory with the following variables:

```env
NOTIFICATION_EMAIL=your-email@gmail.com
NOTIFICATION_EMAIL_PASSWORD=your-app-specific-password
```

> **Note:** For Gmail, you need to generate an App Password if you have 2FA enabled.

### 3. Build and Run with Docker Compose

```bash
docker-compose up --build
```

This will start all required services:
- PostgreSQL database on port 5432
- Kafka on port 9092
- Zookeeper on port 2181
- Auth Service on port 8080
- Notification Service on port 8081

## API Documentation

Once the services are running, you can access the API documentation:

- **Auth Service Swagger UI**: http://localhost:8080/swagger-ui.html
- **Notification Service Swagger UI**: http://localhost:8081/swagger-ui.html

## Default Test Users

Two users are created by default:

1. **Admin User**
   - Username: `admin`
   - Password: `admin123`
   - Role: `ADMIN`

2. **Regular User**
   - Username: `testuser`
   - Password: `admin123`
   - Role: `USER`

## API Endpoints

### Authentication

- **POST /api/auth/signin** - Authenticate and get JWT token
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```

- **POST /api/auth/signup** - Register a new user (public endpoint)
  ```json
  {
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "firstName": "New",
    "lastName": "User"
  }
  ```

### User Management (Requires Authentication)

- **GET /api/users/me** - Get current user profile
- **GET /api/users** - Get all users (ADMIN only)
- **GET /api/users/{id}** - Get user by ID
- **PUT /api/users/{id}** - Update user
- **DELETE /api/users/{id}** - Delete user

## Security

- JWT authentication is required for protected endpoints
- Passwords are hashed using BCrypt
- Role-based access control (USER/ADMIN)
- CSRF protection enabled
- CORS configured

## Email Notifications

The notification service sends emails for the following events:

1. User created
2. User updated
3. User deleted

Emails are sent to all ADMIN users when these events occur.

## Development

### Building Locally

1. Start required services:
   ```bash
   docker-compose up -d postgres kafka
   ```

2. Build and run Auth Service:
   ```bash
   cd auth-service
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. Build and run Notification Service:
   ```bash
   cd notification-service
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Testing

Run tests for each service:

```bash
# In auth-service directory
mvn test

# In notification-service directory
mvn test
```

## Monitoring

- **Actuator Endpoints**:
  - Health: http://localhost:8080/actuator/health
  - Info: http://localhost:8080/actuator/info
  - Metrics: http://localhost:8080/actuator/metrics

## Troubleshooting

1. **Kafka Connection Issues**:
   - Ensure Zookeeper and Kafka are running
   - Check if the Kafka broker is accessible at `localhost:9092`

2. **Database Connection Issues**:
   - Verify PostgreSQL is running
   - Check database credentials in application properties

3. **Email Sending Failures**:
   - Verify email credentials in the `.env` file
   - Check if your email provider allows less secure apps

## License

This project is licensed under the MIT License.
