# 小鱼订单管理系统 (XiaoYu Order System)

基于 Spring Boot 2.7 + MyBatis-Plus 的全栈订单管理系统，支持用户端购物下单和管理端商品/订单/用户管理。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| 持久层 | MyBatis-Plus 3.4.1 |
| 数据库 | MySQL 8.x |
| 连接池 | Druid 1.1.22 |
| 认证 | JWT (jjwt 0.11.5) + Spring Security Crypto (仅密码加密) |
| 参数校验 | Spring Boot Validation |
| 工具库 | Lombok |
| JDK | 17 |

## 项目结构

```
order_system/
├── src/main/java/com/xiaoyu/
│   ├── OrderSystemApplication.java      # 启动类
│   ├── common/                          # 通用组件
│   │   ├── Code.java                    # 状态码常量
│   │   ├── Result.java                  # 统一返回结果封装
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   ├── config/                          # 配置类
│   │   ├── MybatisplusConfig.java       # MyBatis-Plus 配置
│   │   ├── PasswordConfig.java          # 密码加密配置
│   │   ├── SpringMvcConfig.java         # MVC / 拦截器配置
│   │   └── DatabaseSchemaFixer.java     # 数据库Schema自动修复
│   ├── controller/                      # 控制器层
│   │   ├── UserController.java          # 用户认证（登录/注册/登出）
│   │   ├── GoodsController.java         # 商品查询（用户端）
│   │   ├── CartController.java          # 购物车管理
│   │   ├── AddressController.java       # 收货地址管理
│   │   ├── UserOrderController.java     # 用户订单操作
│   │   ├── AdminController.java         # 管理员商品管理
│   │   ├── AdminOrderController.java    # 管理员订单管理
│   │   └── AdminUserController.java     # 管理员用户管理
│   ├── service/                         # 业务逻辑层
│   ├── Dao/                             # 数据访问层
│   ├── pojo/                            # 实体类
│   ├── dto/                             # 数据传输对象
│   ├── vo/                              # 视图对象
│   ├── interceptor/                     # 拦截器
│   │   └── JwtAuthInterceptor.java      # JWT 认证拦截器
│   └── utils/                           # 工具类
│       ├── JwtUtil.java                 # JWT 工具
│       ├── OrderNoutil.java             # 订单号生成
│       └── UserContext.java             # 用户上下文（ThreadLocal）
├── src/main/resources/
│   └── application.yml                  # 应用配置
├── pom.xml                              # Maven 依赖
└── fix_schema.sql                       # 数据库修复脚本
```

## 环境要求

- **JDK** 17 或以上
- **Maven** 3.6+
- **MySQL** 8.0 或以上

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/mcpowerade3912-dev/xiaoyu.git
cd xiaoyu
```

### 2. 创建数据库

在 MySQL 中执行以下命令创建数据库：

```sql
CREATE DATABASE order_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

然后执行项目根目录下的 `fix_schema.sql` 脚本初始化表结构：

```bash
mysql -u root -p order_manager < fix_schema.sql
```

### 3. 修改数据库配置

编辑 `src/main/resources/application.yml`，根据你的环境修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/order_manager?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true
    username: root        # 改为你的数据库用户名
    password: 123456      # 改为你的数据库密码
```

### 4. 编译运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/order_system-0.0.1-SNAPSHOT.jar

# 或使用 Maven 直接运行
mvn spring-boot:run
```

应用默认启动在 **http://localhost:8080**（端口可在 application.yml 中修改）。

## API 接口文档

所有接口返回统一格式：

```json
{
  "code": "200",
  "data": {},
  "msg": "操作描述"
}
```

### 一、用户认证 `/user`

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|:---:|
| POST | `/user/register` | 用户注册 | 否 |
| POST | `/user/login` | 用户登录，返回JWT Token | 否 |
| GET | `/user/logout` | 用户登出 | 是 |

**登录请求体示例：**
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**注册请求体示例：**
```json
{
  "username": "zhangsan",
  "password": "123456",
  "phone": "13800138000"
}
```

> 登录成功后，后续所有需要认证的接口需在请求头中携带 `Authorization: <token>`

### 二、商品浏览 `/goods`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/goods/page` | 商品分页查询 |

**请求参数：** `?pageNum=1&pageSize=10&goodsName=xxx`

### 三、购物车 `/cart`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/cart/add` | 添加商品到购物车 |
| GET | `/cart/get` | 获取购物车列表 |
| PUT | `/cart/update` | 修改购物车商品数量 |

**添加/修改请求体示例：**
```json
{
  "goodsId": 1,
  "num": 2
}
```

### 四、收货地址 `/address`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/address/list` | 获取我的地址列表 |
| GET | `/address/{id}` | 获取地址详情 |
| POST | `/address/add` | 添加收货地址 |
| PUT | `/address/update` | 编辑收货地址 |
| PUT | `/address/default/{id}` | 设置默认地址 |
| DELETE | `/address/{id}` | 删除地址 |

### 五、用户订单 `/order`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/order/create` | 创建订单（结算下单） |
| GET | `/order/page` | 我的订单分页查询 |
| GET | `/order/detail/{orderId}` | 订单详情 |
| PUT | `/order/cancel/{orderId}` | 取消订单 |
| PUT | `/order/confirm/{orderId}` | 确认收货 |

### 六、管理员 - 商品管理 `/admin`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/page` | 管理员分页查询商品 |
| POST | `/admin/add` | 新增商品 |
| PUT | `/admin/update` | 编辑商品 |
| PUT | `/admin/status/{goodsId}` | 商品上下架 |

**上下架参数：** `?status=1` 上架 / `?status=0` 下架

### 七、管理员 - 订单管理 `/admin/order`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/order/page` | 全平台订单分页查询 |
| PUT | `/admin/order/deliver/{orderId}` | 订单发货 |
| GET | `/admin/order/statistics` | 订单数据统计 |

### 八、管理员 - 用户管理 `/admin/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/user/page` | 用户分页查询 |
| PUT | `/admin/user/status/{userId}` | 冻结/解禁用户 |

**状态参数：** `?status=1` 正常 / `?status=0` 冻结

## 认证说明

系统使用 JWT 进行身份认证。登录成功后返回 Token，后续请求需通过请求头传递：

```
Authorization: eyJhbGciOiJIUzI1NiJ9.xxxxx
```

未携带 Token 或 Token 过期时，接口将返回 401 未授权错误。

## 数据库设计

系统包含以下主要数据表：

| 表名 | 说明 |
|------|------|
| `user` | 用户信息表 |
| `goods` | 商品信息表 |
| `user_cart` | 用户购物车表 |
| `user_address` | 收货地址表 |
| `order_main` | 订单主表 |
| `order_item` | 订单明细表 |

## 常见问题

**Q: 启动时报数据库连接失败？**
A: 请确认 MySQL 服务已启动，并检查 `application.yml` 中的数据库地址、用户名和密码是否正确。

**Q: 提示表不存在？**
A: 请先执行 `fix_schema.sql` 初始化数据库表结构，或确认数据库名称为 `order_manager`。

**Q: 端口被占用？**
A: 在 `application.yml` 中添加 `server.port: 8081` 修改启动端口。

## 开发者

- **邮箱**: mcpowerade@gmail.com
- **GitHub**: [mcpowerade3912-dev](https://github.com/mcpowerade3912-dev)

## 许可证

本项目仅供学习交流使用。
