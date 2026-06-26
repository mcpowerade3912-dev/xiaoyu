# XiaoYu Order System

A full-featured e-commerce order management system built with Spring Boot 2.7, MyBatis-Plus, and JWT authentication. It supports a complete shopping workflow including user registration, product browsing, cart management, order placement, and an admin dashboard for managing products, orders, and users.

## Key Features

**User Side**
- User registration, login, and logout with JWT token authentication
- Product browsing with pagination and keyword search
- Shopping cart management (add, update quantity, view)
- Multiple shipping addresses with default address support
- Order lifecycle: create order, cancel order, confirm receipt, view order detail

**Admin Side**
- Product CRUD with shelf status management (on/off)
- Order management with delivery operations and real-time statistics (today's orders, total revenue)
- User management with account freeze/unfreeze capability

**Technical Highlights**
- JWT-based stateless authentication with role-based access control (user vs admin)
- Optimistic locking (`@Version`) for concurrent stock deduction and order status updates
- ThreadLocal-based user context propagation across the request lifecycle
- Global exception handling with unified JSON response format
- Database schema auto-repair on startup

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.4.1 |
| Database | MySQL | 8.x |
| Connection Pool | Druid | 1.1.22 |
| Authentication | JWT (jjwt) | 0.11.5 |
| Password Encryption | Spring Security Crypto | - |
| Parameter Validation | Hibernate Validator | - |
| Build Tool | Maven | 3.6+ |
| Utility | Lombok | 1.18.20 |

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Client Layer                        │
│              (Postman / Frontend / cURL)                 │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────┐
│                   Interceptor Layer                       │
│            JwtAuthInterceptor (JWT Auth + RBAC)           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ 1. Extract JWT from Authorization header             │ │
│  │ 2. Validate token & check user logout status         │ │
│  │ 3. Role check: /admin/** requires role=1             │ │
│  │ 4. Store userId & role in ThreadLocal UserContext    │ │
│  └─────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Controller Layer                        │
│  ┌──────────┬──────────┬──────────┬──────────┬────────┐ │
│  │  User    │  Goods   │  Cart    │ Address  │ Order  │ │
│  │Controller│Controller│Controller│Controller│Controller│ │
│  └──────────┴──────────┴──────────┴──────────┴────────┘ │
│  ┌──────────────────────────────────────────────────┐   │
│  │  AdminController │ AdminOrder │ AdminUser         │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                    Service Layer                          │
│       (Business logic, transactions, validation)          │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                     DAO Layer                             │
│            (MyBatis-Plus Mapper Interface)                │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   MySQL (Druid Pool)                      │
│  ┌────────┬───────┬──────────┬───────────┬────────────┐ │
│  │sys_user│ goods │ user_cart│user_address│ order_main │ │
│  │        │       │          │           │ order_item │ │
│  └────────┴───────┴──────────┴───────────┴────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Project Structure

```
order_system/
├── pom.xml                                          # Maven dependencies
├── fix_schema.sql                                   # Database initialization script
├── README.md
├── docs/
│   └── Address接口测试文档.md
├── test-toolkit/
│   ├── postman/                                     # Postman collection
│   ├── junit5/                                      # JUnit 5 test cases
│   └── jmeter/                                      # JMeter stress test config
└── src/
    ├── main/
    │   ├── java/com/xiaoyu/
    │   │   ├── OrderSystemApplication.java          # Spring Boot entry point
    │   │   │
    │   │   ├── common/                              # Common components
    │   │   │   ├── Code.java                        #   Response status codes
    │   │   │   ├── Result.java                      #   Unified response wrapper
    │   │   │   └── GlobalExceptionHandler.java      #   Global exception handler
    │   │   │
    │   │   ├── config/                              # Configuration
    │   │   │   ├── SpringMvcConfig.java             #   MVC config & interceptor registration
    │   │   │   ├── MybatisplusConfig.java           #   MyBatis-Plus pagination plugin
    │   │   │   ├── PasswordConfig.java              #   BCrypt password encoder bean
    │   │   │   └── DatabaseSchemaFixer.java         #   Auto schema repair on startup
    │   │   │
    │   │   ├── interceptor/
    │   │   │   └── JwtAuthInterceptor.java          # JWT auth + role-based access control
    │   │   │
    │   │   ├── controller/                          # REST API controllers
    │   │   │   ├── UserController.java              #   /user - login, register, logout
    │   │   │   ├── GoodsController.java             #   /goods - product browsing
    │   │   │   ├── CartController.java              #   /cart - shopping cart
    │   │   │   ├── AddressController.java           #   /address - shipping addresses
    │   │   │   ├── UserOrderController.java         #   /order - user order operations
    │   │   │   ├── AdminController.java             #   /admin - product management
    │   │   │   ├── AdminOrderController.java        #   /admin/order - order management
    │   │   │   └── AdminUserController.java         #   /admin/user - user management
    │   │   │
    │   │   ├── service/                             # Business logic
    │   │   │   ├── UserService.java
    │   │   │   ├── GoodsService.java
    │   │   │   ├── CartService.java
    │   │   │   ├── AddressService.java
    │   │   │   ├── OrderService.java
    │   │   │   ├── AdminUserService.java
    │   │   │   └── impl/                            #   Service implementations
    │   │   │
    │   │   ├── Dao/                                 # Data access (MyBatis-Plus Mappers)
    │   │   │   ├── UserDao.java
    │   │   │   ├── GoodsDao.java
    │   │   │   ├── CartDao.java
    │   │   │   ├── UserAddressDao.java
    │   │   │   ├── OrderMainDao.java
    │   │   │   └── OrderItemDao.java
    │   │   │
    │   │   ├── pojo/                                # Database entity classes
    │   │   │   ├── User.java                        #   sys_user table
    │   │   │   ├── Goods.java                       #   goods table
    │   │   │   ├── UserCart.java                     #   user_cart table
    │   │   │   ├── UserAddress.java                 #   user_address table
    │   │   │   ├── Order_main.java                  #   order_main table
    │   │   │   └── Order_item.java                  #   order_item table
    │   │   │
    │   │   ├── dto/                                 # Data Transfer Objects (request)
    │   │   │   ├── LoginDto.java
    │   │   │   ├── UserRegisterDto.java
    │   │   │   ├── GoodsDto.java
    │   │   │   ├── CartDto.java
    │   │   │   ├── AddressDTO.java
    │   │   │   ├── OrderMainDto.java
    │   │   │   ├── OrderItemDto.java
    │   │   │   └── OrderQueryDto.java
    │   │   │
    │   │   ├── vo/                                  # View Objects (response)
    │   │   │   ├── LoginVo.java
    │   │   │   ├── CartVo.java
    │   │   │   ├── OrderDetailVO.java
    │   │   │   └── OrderStatisticsVO.java
    │   │   │
    │   │   └── utils/
    │   │       ├── JwtUtil.java                     # JWT token generation & parsing
    │   │       ├── OrderNoutil.java                 # Unique order number generator
    │   │       └── UserContext.java                 # ThreadLocal user context
    │   │
    │   └── resources/
    │       └── application.yml                      # Application configuration
    │
    └── test/
        └── java/com/xiaoyu/
            ├── OrderSystemApplicationTests.java
            ├── OrderSystemFullTest.java
            └── controller/
                ├── AddressControllerTest.java
                └── AuthControllerTest.java
```

## Database Design

### Entity Relationship

```
sys_user (1) ──────── (N) user_cart (N) ──────── (1) goods
    │
    ├── (1) ──────── (N) user_address
    │
    └── (1) ──────── (N) order_main (1) ──────── (N) order_item (N) ──────── (1) goods
```

### Table Definitions

#### sys_user

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | User ID (snowflake) |
| username | VARCHAR | Unique username |
| password | VARCHAR | BCrypt encrypted password |
| nickname | VARCHAR | Display name |
| phone | VARCHAR | Phone number |
| role | INT | 0=Normal user, 1=Admin |
| status | INT | 0=Frozen, 1=Active |
| create_time | DATETIME | Registration time |
| logout_time | DATETIME | Last logout time (null if logged in) |

#### goods

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Product ID |
| goods_name | VARCHAR | Product name |
| price | DECIMAL(10,2) | Unit price |
| stock | INT | Available stock |
| pic_url | VARCHAR | Product image URL |
| sales | INT | Total sales count |
| version | INT | Optimistic lock version |
| status | INT | 0=Off-shelf, 1=On-shelf |
| create_time | DATETIME | Creation time |
| update_time | DATETIME | Last update time |

#### user_cart

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Cart item ID |
| user_id | BIGINT (FK) | Owner user ID |
| goods_id | BIGINT (FK) | Product ID |
| buy_num | INT | Quantity |
| create_time | DATETIME | Added time |

#### user_address

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Address ID |
| user_id | BIGINT (FK) | Owner user ID |
| name | VARCHAR | Recipient name |
| phone | VARCHAR | Recipient phone |
| address | VARCHAR | Full address |
| is_default | INT | 0=No, 1=Default address |
| create_time | DATETIME | Created time |

#### order_main

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Order ID |
| order_no | VARCHAR | Unique order number |
| user_id | BIGINT (FK) | Buyer user ID |
| total_amount | DECIMAL | Total amount |
| pay_amount | DECIMAL | Actual payment amount |
| status | INT | Order status (see below) |
| pay_time | DATETIME | Payment time |
| delivery_time | DATETIME | Delivery time |
| finish_time | DATETIME | Completion time |
| address_name | VARCHAR | Recipient name (snapshot) |
| address_phone | VARCHAR | Recipient phone (snapshot) |
| address_detail | VARCHAR | Shipping address (snapshot) |
| version | VARCHAR | Optimistic lock version |
| create_time | DATETIME | Order creation time |
| update_time | DATETIME | Last update time |

#### order_item

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Order item ID |
| user_id | BIGINT (FK) | Buyer user ID |
| order_no | VARCHAR | Parent order number |
| goods_id | BIGINT (FK) | Product ID |
| goods_name | VARCHAR | Product name (snapshot) |
| goods_price | DECIMAL | Unit price at purchase (snapshot) |
| buy_num | INT | Quantity purchased |
| create_time | DATETIME | Created time |

### Order Status Flow

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ 0=待付款  │────▶│ 1=待发货  │────▶│ 2=已发货  │────▶│ 3=已完成  │
│ Pending   │     │ Paid     │     │ Shipped  │     │ Finished │
└────┬─────┘     └──────────┘     └──────────┘     └──────────┘
     │
     │ (user cancels)
     ▼
┌──────────┐
│ 4=已取消  │
│ Cancelled│
└──────────┘
```

| Status | Code | Description | Available Actions |
|--------|------|-------------|-------------------|
| 待付款 | 0 | Order created, awaiting payment | User: cancel |
| 待发货 | 1 | Payment confirmed | Admin: deliver |
| 已发货 | 2 | Goods shipped | User: confirm receipt |
| 已完成 | 3 | Transaction complete | - |
| 已取消 | 4 | Cancelled by user | - |

## Quick Start

### Prerequisites

- **JDK 17+**
- **Maven 3.6+**
- **MySQL 8.0+**

### Step 1: Clone the Repository

```bash
git clone https://github.com/mcpowerade3912-dev/xiaoyu.git
cd xiaoyu
```

### Step 2: Set Up the Database

```sql
-- Create the database
CREATE DATABASE order_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

Initialize the tables using the provided script:

```bash
mysql -u root -p order_manager < fix_schema.sql
```

### Step 3: Configure Database Connection

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/order_manager?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true
    username: your_username        # Replace with your MySQL username
    password: your_password        # Replace with your MySQL password
    type: com.alibaba.druid.pool.DruidDataSource

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### Step 4: Build and Run

```bash
# Build the project (skip tests for faster startup)
mvn clean package -DskipTests

# Run with java
java -jar target/order_system-0.0.1-SNAPSHOT.jar

# Or run directly with Maven
mvn spring-boot:run
```

The application starts at **http://localhost:8080** by default.

### Step 5: Verify

```bash
# Check if the server is running
curl http://localhost:8080/goods/page?pageNum=1&pageSize=10
```

## API Reference

All API responses follow the unified format:

```json
{
  "code": "200",
  "data": {},
  "msg": "Operation description"
}
```

**Status Codes:**

| Code | Meaning |
|------|---------|
| 200 | Success |
| 500 | Business error |
| 40001 | Parameter validation failed |
| 40002 | Username already exists |
| 40003 | User does not exist |
| 40004 | Incorrect password |
| 40005 | Account status abnormal (frozen) |
| 401 | Not authenticated (missing/invalid token) |
| 403 | Insufficient permissions (non-admin accessing admin endpoints) |
| 50001 | Internal server error |

### Public Endpoints (No Auth Required)

#### User Registration

```
POST /user/register
Content-Type: application/json
```

**Request:**
```json
{
  "username": "zhangsan",
  "password": "123456",
  "phone": "13800138000"
}
```

**Response (Success):**
```json
{
  "code": "200",
  "data": null,
  "msg": "注册成功"
}
```

**Response (Username Exists):**
```json
{
  "code": "40002",
  "data": null,
  "msg": "用户名已存在"
}
```

#### User Login

```
POST /user/login
Content-Type: application/json
```

**Request:**
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**Response (Success):**
```json
{
  "code": "200",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOjAsInVzZXJuYW1lIjoiemhhbmdzYW4ifQ.xxxxx"
  },
  "msg": "登录成功"
}
```

**Response (Wrong Password):**
```json
{
  "code": "40004",
  "data": null,
  "msg": "密码错误"
}
```

#### Browse Products (No Auth)

```
GET /goods/page?pageNum=1&pageSize=10&goodsName=keyword
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| pageNum | Integer | No | 1 | Page number |
| pageSize | Integer | No | 10 | Items per page |
| goodsName | String | No | - | Filter by product name keyword |

**Response:**
```json
{
  "code": "200",
  "data": {
    "records": [
      {
        "id": 1,
        "goodsName": "iPhone 15",
        "price": 5999.00,
        "stock": 100,
        "pic_url": "https://example.com/iphone15.jpg",
        "sales": 50,
        "status": 1,
        "createTime": "2024-01-15 10:30:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "msg": "查询成功"
}
```

### Authenticated Endpoints

All endpoints below require the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxxx
```

#### User Logout

```
GET /user/logout
Authorization: Bearer <token>
```

**Response:**
```json
{
  "code": "200",
  "data": null,
  "msg": "登出成功"
}
```

> Note: After logout, the token becomes invalid server-side. Subsequent requests with the same token will return 401.

---

#### Shopping Cart

**Add to Cart:**
```
POST /cart/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "goodsId": 1,
  "num": 2
}
```

**View Cart:**
```
GET /cart/get
Authorization: Bearer <token>
```

**Response:**
```json
{
  "code": "200",
  "data": [
    {
      "id": 1,
      "goodsId": 1,
      "goodsName": "iPhone 15",
      "goodsPrice": 5999.00,
      "picUrl": "https://example.com/iphone15.jpg",
      "buyNum": 2,
      "subtotal": 11998.00
    }
  ],
  "msg": "查询成功"
}
```

**Update Quantity:**
```
PUT /cart/update
Authorization: Bearer <token>
Content-Type: application/json

{
  "goodsId": 1,
  "num": 5
}
```

---

#### Shipping Addresses

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/address/list` | List all my addresses |
| GET | `/address/{id}` | Get address detail |
| POST | `/address/add` | Add new address |
| PUT | `/address/update` | Edit address |
| PUT | `/address/default/{id}` | Set as default address |
| DELETE | `/address/{id}` | Delete address |

**Add Address:**
```json
POST /address/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Zhang San",
  "phone": "13800138000",
  "address": "Beijing, Haidian District, Zhongguancun Street No.1"
}
```

---

#### User Orders

**Create Order:**
```
POST /order/create
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "addressName": "Zhang San",
  "addressPhone": "13800138000",
  "addressDetail": "Beijing, Haidian District, Zhongguancun Street No.1",
  "goodsList": [
    { "goodsId": 1, "buyNum": 2 },
    { "goodsId": 3, "buyNum": 1 }
  ]
}
```

**Response (Success):**
```json
{
  "code": "200",
  "data": 1,
  "msg": "下单成功"
}
```

**Response (Insufficient Stock):**
```json
{
  "code": "500",
  "data": null,
  "msg": "商品iPhone 15库存不足"
}
```

> The order creation process uses optimistic locking for stock deduction. Under high concurrency, stock conflicts will return a "please refresh" message.

**My Orders (Paginated):**
```
GET /order/page?pageNum=1&pageSize=10&status=0
Authorization: Bearer <token>
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Page size (default: 10) |
| status | Integer | No | Filter by order status (0-4) |

**Order Detail:**
```
GET /order/detail/{orderId}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "code": "200",
  "data": {
    "id": 1,
    "orderNo": "ORD20240115103000001",
    "totalAmount": 17997.00,
    "payAmount": 17997.00,
    "status": 0,
    "addressName": "Zhang San",
    "addressPhone": "13800138000",
    "addressDetail": "Beijing, Haidian District",
    "itemList": [
      {
        "goodsId": 1,
        "goodsName": "iPhone 15",
        "goodsPrice": 5999.00,
        "buyNum": 2
      },
      {
        "goodsId": 3,
        "goodsName": "AirPods Pro",
        "goodsPrice": 1999.00,
        "buyNum": 1
      }
    ]
  },
  "msg": "查询成功"
}
```

**Cancel Order:**
```
PUT /order/cancel/{orderId}
Authorization: Bearer <token>
```

> Only orders with status `0 (Pending)` can be cancelled. Stock is automatically restored upon cancellation.

**Confirm Receipt:**
```
PUT /order/confirm/{orderId}
Authorization: Bearer <token>
```

> Only orders with status `2 (Shipped)` can be confirmed.

---

### Admin Endpoints

> All `/admin/**` endpoints require the user to have `role=1` (admin). Non-admin users will receive a `403 Forbidden` response.

#### Product Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/admin/page` | List products (paginated) |
| POST | `/admin/add` | Add new product |
| PUT | `/admin/update` | Edit product |
| PUT | `/admin/status/{goodsId}` | Toggle shelf status |

**Add Product:**
```json
POST /admin/add
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "goodsName": "MacBook Pro 14",
  "price": 14999.00,
  "stock": 50,
  "pic_url": "https://example.com/macbook.jpg"
}
```

**Toggle Shelf Status:**
```
PUT /admin/status/1?status=0     # Take off shelf
PUT /admin/status/1?status=1     # Put on shelf
```

#### Order Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/order/page` | List all orders (paginated) |
| PUT | `/admin/order/deliver/{orderId}` | Mark order as delivered |
| GET | `/admin/order/statistics` | Get sales statistics |

**Sales Statistics Response:**
```json
{
  "code": "200",
  "data": {
    "todayOrderCount": 23,
    "todayTotalAmount": 156800.00,
    "totalOrderCount": 1520,
    "totalAmount": 8950000.00
  },
  "msg": "查询成功"
}
```

**Deliver Order:**
```
PUT /admin/order/deliver/{orderId}
Authorization: Bearer <admin-token>
```

> Only orders with status `1 (Paid)` can be delivered.

#### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/user/page` | List users (paginated) |
| PUT | `/admin/user/status/{userId}` | Freeze/unfreeze user |

**List Users:**
```
GET /admin/user/page?pageNum=1&pageSize=10&username=zhang
```

**Freeze/Unfreeze User:**
```
PUT /admin/user/status/1?status=0    # Freeze user
PUT /admin/user/status/1?status=1    # Unfreeze user
```

> Frozen users cannot log in. If a logged-in user is frozen, their subsequent requests will be rejected.

## Security

### Authentication Flow

```
Client                          Server
  │                                │
  │  POST /user/login              │
  │  {username, password}          │
  │ ──────────────────────────────▶│
  │                                │ 1. Validate credentials
  │                                │ 2. Generate JWT (contains userId, role, username)
  │  ◀────────────────────────────│ 3. Return token
  │  {token: "eyJhbG..."}         │
  │                                │
  │  GET /order/page               │
  │  Authorization: Bearer <token> │
  │ ──────────────────────────────▶│
  │                                │ 1. JwtAuthInterceptor intercepts
  │                                │ 2. Parse & validate JWT
  │                                │ 3. Check user.logoutTime == null
  │                                │ 4. For /admin/**: check role == 1
  │                                │ 5. Store userId in ThreadLocal
  │  ◀────────────────────────────│ 6. Forward to controller
  │  {data: [...]}                 │
```

### Key Security Mechanisms

- **Password Encryption:** Passwords are hashed using BCrypt (via Spring Security Crypto). Plaintext passwords are never stored.
- **JWT Stateless Auth:** Tokens are self-contained with `userId`, `role`, and `username`. No server-side session storage.
- **Logout Invalidation:** On logout, a `logoutTime` is recorded in the database. The interceptor checks this field on every request, effectively invalidating the token without maintaining a blacklist.
- **Role-Based Access Control:** Admin endpoints (`/admin/**`) are protected at the interceptor level. Non-admin users receive `403 Forbidden`.
- **Optimistic Locking:** `@Version` annotation on `goods.version` and `order_main.version` prevents race conditions during concurrent stock deduction and order status updates.

## Testing

The project includes a comprehensive test toolkit under `test-toolkit/`:

### JUnit 5 Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=OrderSystemFullTest
```

### Postman Collection

Import the collection file into Postman:

```
test-toolkit/postman/order_system_api_test.postman_collection.json
```

The collection includes pre-configured requests for all endpoints with environment variables for the base URL and auth token.

### JMeter Stress Test

See `test-toolkit/jmeter/JMeter压测配置指南.md` for detailed load testing instructions.

## Deployment

### Building a Production JAR

```bash
mvn clean package -DskipTests
```

The JAR file will be generated at `target/order_system-0.0.1-SNAPSHOT.jar`.

### Running in Production

```bash
# Basic
java -jar order_system-0.0.1-SNAPSHOT.jar

# With custom port
java -jar order_system-0.0.1-SNAPSHOT.jar --server.port=9090

# With custom database
java -jar order_system-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://your-db-host:3306/order_manager \
  --spring.datasource.username=prod_user \
  --spring.datasource.password=secure_password

# Background mode with log output
nohup java -jar order_system-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### Recommended Production Configuration

Add to `application.yml` for production:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://your-db-host:3306/order_manager?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl  # Disable SQL logging in production
```

## Troubleshooting

**Database connection failed on startup**
- Verify MySQL is running: `systemctl status mysql` or `brew services list`
- Check credentials in `application.yml`
- Ensure the `order_manager` database exists

**Table 'order_manager.xxx' doesn't exist**
- Run `fix_schema.sql` to initialize tables: `mysql -u root -p order_manager < fix_schema.sql`

**Port 8080 already in use**
- Change the port: add `server.port=8081` to `application.yml`
- Or kill the process: `lsof -i :8080` then `kill -9 <PID>`

**Token returns 401 after login**
- Ensure the header format is `Authorization: Bearer <token>` (with "Bearer " prefix)
- Check if the user has been logged out (logout invalidates the token server-side)
- Verify the token has not been tampered with

**Order creation fails with "库存不足" (Insufficient stock)**
- The requested quantity exceeds available stock
- Under high concurrency, another user may have purchased the last items
- Retry the request after refreshing

**Admin endpoints return 403**
- The logged-in user does not have admin privileges (role != 1)
- Update the user's role in the database: `UPDATE sys_user SET role = 1 WHERE username = 'your_admin';`

## Contact

- **Email:** mcpowerade3912@gmail.com
- **GitHub:** [mcpowerade3912-dev](https://github.com/mcpowerade3912-dev)

## License

This project is for educational and learning purposes only.
