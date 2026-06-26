# Address 接口测试文档

## 1. 概述

本文档记录订单系统中 **地址管理模块** 的接口测试方案与测试结果。

- **测试框架**：JUnit 5 + Spring Boot Test + Mockito
- **测试方式**：`@WebMvcTest` Controller 层单元测试，Mock Service 层依赖
- **测试类**：`com.xiaoyu.controller.AddressControllerTest`
- **测试文件**：`src/test/java/com/xiaoyu/controller/AddressControllerTest.java`
- **测试结果**：✅ 18/18 全部通过

---

## 2. 接口清单

| 序号 | HTTP 方法 | 路径                  | 功能         | 需要认证 |
|------|-----------|----------------------|-------------|---------|
| 1    | POST      | `/address/add`        | 添加地址      | 是      |
| 2    | PUT       | `/address/update`     | 编辑地址      | 是      |
| 3    | DELETE    | `/address/{id}`       | 删除地址      | 是      |
| 4    | PUT       | `/address/default/{id}` | 设置默认地址  | 是      |
| 5    | GET       | `/address/list`       | 查询地址列表   | 是      |
| 6    | GET       | `/address/{id}`       | 查询地址详情   | 是      |

---

## 3. 请求/响应数据结构

### 3.1 AddressDTO（请求体）

| 字段       | 类型     | 必填 | 校验规则                        | 说明               |
|-----------|----------|------|-------------------------------|-------------------|
| id        | Long     | 否   | —                             | 编辑时传入，新增时不传 |
| name      | String   | 是   | `@NotBlank`                   | 收件人姓名          |
| phone     | String   | 是   | `@NotBlank` + `@Pattern(^1[3-9]\d{9}$)` | 手机号              |
| address   | String   | 是   | `@NotBlank`                   | 详细地址            |
| isDefault | Integer  | 否   | 默认值 0                        | 是否默认地址（0否/1是）|

### 3.2 Result（响应体）

| 字段   | 类型     | 说明                |
|--------|----------|-------------------|
| code   | String   | 状态码：`200`成功，`500`失败 |
| msg    | String   | 提示信息            |
| data   | Object   | 返回数据            |

---

## 4. 测试用例详情

### 4.1 添加地址 `POST /address/add`

#### TC-ADD-001：成功添加地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证正常参数可以成功添加地址 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"name":"张三","phone":"13800138000","address":"北京市朝阳区xxx街道","isDefault":0}` |
| 预期结果 | HTTP 200，`code=200`，`msg="地址新增成功"` |
| 测试结果 | ✅ 通过 |

#### TC-ADD-002：添加为默认地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证 `isDefault=1` 时可成功添加默认地址 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"name":"张三","phone":"13800138000","address":"北京市朝阳区xxx街道","isDefault":1}` |
| 预期结果 | HTTP 200，`code=200` |
| 测试结果 | ✅ 通过 |

#### TC-ADD-003：收件人姓名为空

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证 `@NotBlank` 校验生效 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"name":"","phone":"13800138000","address":"北京市朝阳区xxx街道","isDefault":0}` |
| 预期结果 | HTTP 400（参数校验失败） |
| 测试结果 | ✅ 通过 |

#### TC-ADD-004：手机号格式无效

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证手机号正则校验 `^1[3-9]\d{9}$` 生效 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"name":"张三","phone":"12345","address":"北京市朝阳区xxx街道","isDefault":0}` |
| 预期结果 | HTTP 400（参数校验失败） |
| 测试结果 | ✅ 通过 |

#### TC-ADD-005：详细地址为空

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证地址字段 `@NotBlank` 校验生效 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"name":"张三","phone":"13800138000","address":"","isDefault":0}` |
| 预期结果 | HTTP 400（参数校验失败） |
| 测试结果 | ✅ 通过 |

#### TC-ADD-006：未登录添加地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证未携带 Token 时被拦截器拦截 |
| 请求头   | 无 `Authorization` |
| 请求体   | 正常参数 |
| 预期结果 | 返回 `code=401`，`msg="未登录，请登录"` |
| 测试结果 | ✅ 通过 |

---

### 4.2 编辑地址 `PUT /address/update`

#### TC-UPD-001：成功编辑地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证传入有效 id 和参数可成功修改地址 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"id":1,"name":"张三","phone":"13800138000","address":"北京市朝阳区xxx街道","isDefault":0}` |
| 预期结果 | HTTP 200，`code=200`，`msg="地址修改成功"` |
| 测试结果 | ✅ 通过 |

#### TC-UPD-002：地址 ID 为空

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证不传 id 时 Service 层返回失败 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"name":"张三","phone":"13800138000","address":"北京市朝阳区xxx街道","isDefault":0}` |
| 预期结果 | HTTP 200，`code=500`，`msg="地址id不能为空"` |
| 测试结果 | ✅ 通过 |

#### TC-UPD-003：地址不存在

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证编辑不存在的地址返回失败 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"id":999,"name":"张三","phone":"13800138000","address":"北京市朝阳区xxx街道","isDefault":0}` |
| 预期结果 | HTTP 200，`code=500`，`msg="地址不存在"` |
| 测试结果 | ✅ 通过 |

#### TC-UPD-004：编辑时姓名为空

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证编辑时参数校验仍然生效 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 请求体   | `{"id":1,"name":"","phone":"13800138000","address":"北京市朝阳区xxx街道","isDefault":0}` |
| 预期结果 | HTTP 400（参数校验失败） |
| 测试结果 | ✅ 通过 |

---

### 4.3 删除地址 `DELETE /address/{id}`

#### TC-DEL-001：成功删除地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证删除存在的地址成功 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 路径参数 | `id=1` |
| 预期结果 | HTTP 200，`code=200`，`msg="删除成功"` |
| 测试结果 | ✅ 通过 |

#### TC-DEL-002：删除不存在的地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证删除不存在或无权限的地址返回失败 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 路径参数 | `id=999` |
| 预期结果 | HTTP 200，`code=500`，`msg="地址不存在或无权限"` |
| 测试结果 | ✅ 通过 |

---

### 4.4 设置默认地址 `PUT /address/default/{id}`

#### TC-DEF-001：成功设置默认地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证可将指定地址设为默认 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 路径参数 | `id=1` |
| 预期结果 | HTTP 200，`code=200`，`msg="设置默认地址成功"` |
| 测试结果 | ✅ 通过 |

#### TC-DEF-002：设置不存在的地址为默认

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证设置不存在的地址返回失败 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 路径参数 | `id=999` |
| 预期结果 | HTTP 200，`code=500`，`msg="地址不存在"` |
| 测试结果 | ✅ 通过 |

---

### 4.5 查询地址列表 `GET /address/list`

#### TC-LIST-001：查询多条地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证返回当前用户的地址列表，按默认优先+创建时间倒序 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 预期结果 | HTTP 200，`code=200`，`data` 数组长度为 2，默认地址排首位 |
| 测试结果 | ✅ 通过 |

#### TC-LIST-002：地址列表为空

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证用户无地址时返回空数组 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 预期结果 | HTTP 200，`code=200`，`data=[]` |
| 测试结果 | ✅ 通过 |

---

### 4.6 查询地址详情 `GET /address/{id}`

#### TC-GET-001：成功查询地址详情

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证通过 id 查询地址返回完整信息 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 路径参数 | `id=1` |
| 预期结果 | HTTP 200，`code=200`，`data` 包含 name/phone/address 等字段 |
| 测试结果 | ✅ 通过 |

#### TC-GET-002：查询不存在的地址

| 项目     | 内容 |
|---------|------|
| 测试目的 | 验证查询不存在的地址返回失败 |
| 请求头   | `Authorization: Bearer <valid_token>` |
| 路径参数 | `id=999` |
| 预期结果 | HTTP 200，`code=500`，`msg="地址不存在"` |
| 测试结果 | ✅ 通过 |

---

## 5. 测试执行结果

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.xiaoyu.controller.AddressControllerTest
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
Time elapsed: 2.216 s

BUILD SUCCESS
```

| 指标     | 数值   |
|---------|--------|
| 总用例数  | 18     |
| 通过     | 18     |
| 失败     | 0      |
| 错误     | 0      |
| 跳过     | 0      |
| 通过率   | 100%   |
| 执行时间  | ~2.2s  |

---

## 6. 测试策略说明

### 6.1 测试分层

采用 **Controller 层单元测试**，通过 `@WebMvcTest` 仅加载 Web 层：

```
┌─────────────────────────────────────┐
│         AddressControllerTest        │  ← 测试层
│  @WebMvcTest + MockMvc              │
├─────────────────────────────────────┤
│  @MockBean AddressService           │  ← Mock 依赖
│  @MockBean JwtUtil                  │
│  @MockBean UserDao                  │
├─────────────────────────────────────┤
│  JwtAuthInterceptor (真实加载)       │  ← 拦截器真实执行
│  SpringMvcConfig (真实加载)          │
└─────────────────────────────────────┘
```

### 6.2 认证模拟

通过 Mock `JwtUtil` 和 `UserDao` 模拟完整的 JWT 认证链路：

1. Mock `JwtUtil.parseToken()` 返回包含 `userId=1, role=0` 的 Claims
2. Mock `UserDao.selectById()` 返回一个 `logoutTime=null` 的用户（表示在线状态）
3. 请求头携带 `Authorization: Bearer valid_jwt_token`

### 6.3 未覆盖场景

以下场景建议后续补充集成测试（`@SpringBootTest`）：

| 场景 | 原因 |
|------|------|
| 跨用户攻击（操作他人地址） | 依赖 Service 层归属校验，当前 Mock 了 Service |
| 设置默认地址时重置其他地址 | 依赖数据库事务，需集成测试 |
| 并发添加默认地址 | 需要多线程集成测试 |
| Token 过期/用户已登出 | 依赖数据库查询用户状态 |

---

## 7. 运行方式

```bash
# 确保使用 JDK 17
export JAVA_HOME="/c/Program Files/Java/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"

# 运行 Address 测试
cd "D:\Java code\order_system"
mvn test -Dtest=AddressControllerTest

# 运行所有测试
mvn test
```
