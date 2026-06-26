# JMeter 压测配置指南 - 订单管理系统

---

## 一、测试前准备

### 1.1 安装 JMeter
- 下载: https://jmeter.apache.org/download.cgi (推荐 5.5+)
- 启动: `bin/jmeter.bat`(Windows) 或 `bin/jmeter.sh`(Mac/Linux)

### 1.2 确保被测系统已启动
```bash
# 启动 order_system 应用，确认端口 8080 可访问
curl http://localhost:8080/goods/page?pageNum=1&pageSize=1
```

---

## 二、压测场景配置

### 场景1: TC-STRESS-001 登录接口压测

**目的**: 测试登录接口在高并发下的响应能力

**JMeter 配置**:

| 配置项 | 值 |
|--------|-----|
| 线程数 | 100 |
| Ramp-Up | 10秒 |
| 循环次数 | 10 |
| 总请求数 | 1000 |

**HTTP 请求**:
- 方法: POST
- 路径: /user/login
- Header: Content-Type: application/json
- Body:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**校验标准**:
- 平均响应时间 < 200ms
- 99%请求响应时间 < 500ms
- 错误率 < 1%
- 吞吐量 > 100 req/sec

---

### 场景2: TC-STRESS-002 商品查询压测(免登录)

**目的**: 测试商品列表查询在高并发下的性能

**JMeter 配置**:

| 配置项 | 值 |
|--------|-----|
| 线程数 | 200 |
| Ramp-Up | 10秒 |
| 循环次数 | 20 |
| 总请求数 | 4000 |

**HTTP 请求**:
- 方法: GET
- 路径: /goods/page?pageNum=1&pageSize=10

**校验标准**:
- 平均响应时间 < 100ms
- 99%请求响应时间 < 300ms
- 错误率 = 0%
- 吞吐量 > 500 req/sec

---

### 场景3: TC-STRESS-003 混合场景压测

**目的**: 模拟真实用户行为，同时执行多种操作

**线程组配置**:

| 线程组 | 线程数 | 循环次数 | 说明 |
|--------|--------|---------|------|
| 登录用户 | 20 | 5 | 先登录获取Token |
| 浏览商品 | 50 | 20 | 查询商品列表 |
| 下单用户 | 10 | 3 | 创建订单 |
| 查看订单 | 20 | 10 | 查询订单列表 |

**执行步骤**:
1. 创建 CSV Data Set Config，存放测试用户账号密码
2. 登录线程组执行后，用正则提取器提取 Token
3. 通过 User Defined Variables 传递 Token 给其他线程组
4. 各线程组并行执行

**校验标准**:
- 总吞吐量 > 200 req/sec
- 登录平均响应时间 < 300ms
- 查询平均响应时间 < 150ms
- 下单平均响应时间 < 500ms
- 错误率 < 2%

---

### 场景4: TC-STRESS-004 Token 失效压测

**目的**: 验证登出后Token失效机制在并发下的正确性

**执行步骤**:
1. 线程组A: 登录 → 登出 (标记Token失效)
2. 线程组B: 用已失效Token并发访问受保护接口
3. 观察是否全部返回401

**校验标准**:
- 失效Token请求 100% 返回 code=401
- 无脏数据产生

---

## 三、JMeter 元件配置要点

### 3.1 HTTP 请求默认值
```
协议: http
服务器: localhost
端口: 8080
Content-Type: application/json
```

### 3.2 HTTP Header 管理器
```
Authorization: Bearer ${token}
Content-Type: application/json
```

### 3.3 正则提取器(提取Token)
```
引用名称: token
正则表达式: "token":"(.+?)"
模板: $1$
匹配数字: 1
```

### 3.4 响应断言
```
测试字段: Response Code
模式匹配: Equals
测试值: 200
```

### 3.5 聚合报告关键指标

| 指标 | 含义 | 关注阈值 |
|------|------|---------|
| Average | 平均响应时间 | < 200ms |
| 90% Line | 90%请求的响应时间 | < 300ms |
| 99% Line | 99%请求的响应时间 | < 500ms |
| Throughput | 每秒处理请求数 | > 100/sec |
| Error % | 错误率 | < 1% |

---

## 四、数据库验证

压测完成后执行以下SQL验证数据一致性:

```sql
-- 检查订单数据完整性
SELECT COUNT(*) FROM order_main WHERE create_time > DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- 检查无异常库存扣减
SELECT id, goods_name, stock, sales FROM goods WHERE stock < 0;

-- 检查购物车数据
SELECT COUNT(*) FROM user_cart;

-- 检查是否有重复订单号
SELECT order_no, COUNT(*) as cnt FROM order_main GROUP BY order_no HAVING cnt > 1;
```

---

## 五、命令行非GUI运行

```bash
# 执行压测并生成报告
jmeter -n -t order_system_stress.jmx -l result.jtl -e -o ./report

# 参数说明:
# -n: 非GUI模式(性能更好)
# -t: 测试计划文件
# -l: 结果日志
# -e: 生成HTML报告
# -o: 报告输出目录
```
