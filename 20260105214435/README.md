# Apache Ignite Spring Boot Demo

A Spring Boot 3.2.0 integration demo with Apache Ignite 2.16.0 for in-memory data caching.

## Project Overview

This demo demonstrates how to integrate Apache Ignite with Spring Boot to create a high-performance in-memory data grid with REST API endpoints. The application uses Ignite in **embedded mode** for local development and testing.

## Technologies Used

- **Java**: 17.0.10
- **Spring Boot**: 3.2.0
- **Spring Framework**: 6.1.1
- **Apache Ignite**: 2.16.0 (Embedded Mode)
- **Maven**: 3.9.2
- **Build Tool**: Maven

## Features

- **In-Memory Caching**: High-performance distributed in-memory data grid
- **REST API**: Complete CRUD operations for Users and Products
- **Data Filtering**: Support for filtering by various criteria (username, email, age range, category, price range)
- **Auto-Initialization**: Sample data automatically loaded on startup
- **ScanQuery Support**: Efficient in-memory data querying without SQL indexing

## Project Structure

```
ignite-spring-boot-demo/
├── src/main/java/com/demo/ignite/
│   ├── config/
│   │   └── IgniteConfig.java          # Ignite configuration
│   ├── entity/
│   │   ├── User.java                  # User entity
│   │   └── Product.java              # Product entity
│   ├── repository/
│   │   ├── UserRepository.java         # User data access
│   │   └── ProductRepository.java     # Product data access
│   ├── service/
│   │   ├── UserService.java           # User business logic
│   │   └── ProductService.java       # Product business logic
│   ├── controller/
│   │   ├── UserController.java        # User REST endpoints
│   │   ├── ProductController.java    # Product REST endpoints
│   │   └── TestController.java      # Test and demo endpoints
│   ├── DataInitializer.java            # Sample data loader
│   └── IgniteSpringBootApplication.java  # Main application class
├── src/main/resources/
│   ├── application.properties           # Application configuration
│   └── ignite-config.xml            # Ignite XML config (optional)
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

## Prerequisites

- **JDK 17** or higher
- **Maven 3.6+**
- **512MB minimum heap memory** (Ignite requirement)

## Quick Start

### 1. Build the Project

```bash
mvn clean compile
```

### 2. Run the Application

#### Option 1: Using Maven (Recommended)

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms512m -Xmx1024m -Djava.net.preferIPv4Stack=true --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/sun.net.util=ALL-UNNAMED"
```

#### Option 2: Using Batch File (Windows)

```bash
final-run.bat
```

### 3. Verify the Application

Once started, the application will be available at:

- **Application URL**: http://localhost:8080
- **Status Endpoint**: http://localhost:8080/api/test/status

## API Endpoints

### Test Endpoints

#### Get Application Status
```http
GET /api/test/status
```

**Response Example:**
```json
{
  "application": "Ignite Spring Boot Demo",
  "status": "running",
  "totalUsers": 5,
  "totalProducts": 8,
  "description": "Apache Ignite in-memory SQL cache demo"
}
```

### User Endpoints

#### Get All Users
```http
GET /api/users
```

#### Get User by ID
```http
GET /api/users/{id}
```

#### Create User
```http
POST /api/users
Content-Type: application/json

{
  "id": 6,
  "username": "new_user",
  "email": "new@example.com",
  "fullName": "New User",
  "bio": "Test user",
  "age": 25,
  "active": true,
  "createdAt": 1704460800000
}
```

#### Find Users by Username
```http
GET /api/users/search/username/{username}
```

#### Find Users by Email
```http
GET /api/users/search/email/{email}
```

#### Find Active Users
```http
GET /api/users/search/active
```

#### Find Users by Age Range
```http
GET /api/users/search/age-range?minAge=25&maxAge=35
```

#### Count Users
```http
GET /api/users/count
```

#### Delete User
```http
DELETE /api/users/{id}
```

### Product Endpoints

#### Get All Products
```http
GET /api/products
```

#### Get Product by ID
```http
GET /api/products/{id}
```

#### Create Product
```http
POST /api/products
Content-Type: application/json

{
  "id": 9,
  "name": "New Product",
  "description": "Test product",
  "price": 99.99,
  "category": "Electronics",
  "stock": 50,
  "available": true,
  "createdAt": 1704460800000
}
```

#### Find Products by Category
```http
GET /api/products/search/category/{category}
```

#### Find Available Products
```http
GET /api/products/search/available
```

#### Find Products by Price Range
```http
GET /api/products/search/price-range?minPrice=10&maxPrice=100
```

#### Count Products
```http
GET /api/products/count
```

#### Delete Product
```http
DELETE /api/products/{id}
```

## Sample Data

The application automatically initializes with the following sample data:

### Users (5 records)
1. John Doe (john_doe)
2. Jane Smith (jane_smith)
3. Bob Wilson (bob_wilson)
4. Alice Brown (alice_brown) - inactive
5. Charlie Davis (charlie_davis)

### Products (8 records)
1. Laptop - $1299.99 (Electronics)
2. Wireless Mouse - $29.99 (Accessories)
3. Mechanical Keyboard - $149.99 (Accessories)
4. Monitor Stand - $79.99 (Accessories)
5. USB-C Hub - $49.99 (Accessories)
6. Webcam - $89.99 (Electronics)
7. Desk Lamp - $39.99 (Office Supplies)
8. Notebook Set - $19.99 (Office Supplies)

## Configuration

### Application Properties (application.properties)

```properties
# Server Configuration
server.port=8080
spring.application.name=ignite-spring-boot-demo

# Apache Ignite Configuration
ignite.config-file=classpath:ignite-config.xml

# Logging Configuration
logging.level.root=INFO
logging.level.com.demo=DEBUG
logging.level.org.apache.ignite=WARN
```

### JVM Arguments (Required for Java 17+)

Due to Java 9+ module system restrictions, the following JVM arguments are required:

```bash
--add-opens java.base/java.nio=ALL-UNNAMED
--add-opens java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED
--add-opens java.base/java.text=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/java.util.concurrent=ALL-UNNAMED
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.time=ALL-UNNAMED
--add-opens java.base/java.math=ALL-UNNAMED
--add-opens java.base/sun.net.util=ALL-UNNAMED
-Xms512m -Xmx1024m
-Djava.net.preferIPv4Stack=true
```

## Ignite Configuration

### Key Configuration Settings

- **Mode**: Embedded (single node)
- **Peer Class Loading**: Enabled
- **Marshaller**: JDK Marshaller (for Java 17 compatibility)
- **Atomicity Mode**: ATOMIC
- **Backups**: 1
- **Caches**:
  - UserCache (Key: Long, Value: User)
  - ProductCache (Key: Long, Value: Product)

### Data Querying

The demo uses **ScanQuery** for data filtering instead of SQL indexing. This approach:

- Avoids H2 database dependency issues with Spring Boot classloader
- Provides in-memory filtering without SQL overhead
- Works seamlessly with Java 17+ module system

## Testing the Application

### Using curl

```bash
# Get application status
curl http://localhost:8080/api/test/status

# Get all users
curl http://localhost:8080/api/users

# Get active users
curl http://localhost:8080/api/users/search/active

# Find users by age range
curl "http://localhost:8080/api/users/search/age-range?minAge=30&maxAge=35"

# Get all products
curl http://localhost:8080/api/products

# Find products by category
curl http://localhost:8080/api/products/search/category/Electronics
```

### Using PowerShell

```powershell
# Get application status
Invoke-RestMethod -Uri 'http://localhost:8080/api/test/status' -Method Get

# Get all users
Invoke-RestMethod -Uri 'http://localhost:8080/api/users' -Method Get

# Create a new user
$body = @{
    id = 10
    username = "test_user"
    email = "test@example.com"
    fullName = "Test User"
    bio = "Demo user"
    age = 30
    active = $true
    createdAt = [long](Get-Date -UFormat %s) * 1000
} | ConvertTo-Json

Invoke-RestMethod -Uri 'http://localhost:8080/api/users' -Method Post -Body $body -ContentType 'application/json'
```

## Troubleshooting

### Common Issues

#### 1. ClassNotFoundException: org.h2.value.ValueByte

**Cause**: Ignite's SQL indexing requires H2, but Spring Boot's classloader has issues.

**Solution**: This demo uses ScanQuery instead of SQL queries to avoid this issue.

#### 2. InaccessibleObjectException: Unable to make field accessible

**Cause**: Java 9+ module system restricts access to internal APIs.

**Solution**: Add the required `--add-opens` JVM arguments (see "JVM Arguments" section).

#### 3. Initial heap size is less than 512MB

**Cause**: Ignite requires minimum 512MB heap.

**Solution**: Set `-Xms512m -Xmx1024m` JVM arguments.

#### 4. Port 8080 already in use

**Solution**: Change `server.port` in `application.properties`.

## Building for Production

```bash
# Create executable JAR
mvn clean package -DskipTests

# Run the JAR
java -Xms512m -Xmx1024m -Djava.net.preferIPv4Stack=true \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens java.base/java.text=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.time=ALL-UNNAMED \
  --add-opens java.base/java.math=ALL-UNNAMED \
  --add-opens java.base/sun.net.util=ALL-UNNAMED \
  -jar target/ignite-spring-boot-demo-1.0.0.jar
```

## References

- [Apache Ignite Documentation](https://ignite.apache.org/docs/latest/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Ignite Java 17+ Compatibility](https://ignite.apache.org/docs/latest/installation-guide.html)

## License

This demo project is provided as-is for educational purposes.

## Contact

For questions or issues, please refer to the Apache Ignite community resources.
