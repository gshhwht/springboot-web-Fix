# Quick Start Guide

## Prerequisites
- Java 11 or higher installed
- Maven installed

## Run the Application

1. Navigate to the project directory:
```bash
cd vulnerable-spring-boot
```

2. Build and run:
```bash
mvn spring-boot:run
```

3. The application will start on `http://localhost:8080`

## Test the Vulnerabilities

### Test SQL Injection
```bash
curl "http://localhost:8080/api/user?username=admin"
curl "http://localhost:8080/api/user?username=admin' OR '1'='1"
```

### Test Command Injection (Windows)
```bash
curl "http://localhost:8080/api/ping?host=127.0.0.1"
```

### Test Hardcoded Credentials
```bash
curl -X POST "http://localhost:8080/api/admin/login?username=admin&password=admin123"
```

### Test Sensitive Data Exposure
```bash
curl "http://localhost:8080/api/config"
```

### Access H2 Console
Open browser: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

### View Actuator Endpoints
```bash
curl "http://localhost:8080/actuator"
curl "http://localhost:8080/actuator/health"
curl "http://localhost:8080/actuator/env"
```

## Stop the Application
Press `Ctrl+C` in the terminal

## Important Notes
⚠️ This application is intentionally vulnerable. Use only for learning and testing in isolated environments!