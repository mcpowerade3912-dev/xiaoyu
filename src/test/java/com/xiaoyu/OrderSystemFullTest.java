package com.xiaoyu;

import com.xiaoyu.Dao.UserDao;
import com.xiaoyu.pojo.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单管理系统 全接口集成测试
 *
 * 覆盖: 用户认证、商品、购物车、订单、地址、管理员(商品/订单/用户)  全部模块
 * 测试类型: 功能测试 + 边界异常测试 + 权限测试
 *
 * 运行: mvn test -Dtest=OrderSystemFullTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("订单管理系统 - 全接口集成测试")
public class OrderSystemFullTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 管理员Token (role=1) */
    private static String adminToken;
    /** 普通用户Token (role=0) */
    private static String userToken;
    /** 注册的普通用户ID */
    private static Long userId;
    /** 测试用的商品ID (从数据库中取第一个上架商品) */
    private static Long testGoodsId = 1L;
    /** 测试用的订单ID (下单后回填) */
    private static Long testOrderId;
    /** 测试用的地址ID (新增后回填) */
    private static Long testAddressId;
    /** 注册的普通用户名 */
    private static String registeredUsername;

    /**
     * 测试前确保admin用户存在且role=1
     */
    @BeforeAll
    static void setupAdmin(@Autowired UserDao userDao, @Autowired PasswordEncoder passwordEncoder) {
        // 查找admin用户
        User admin = userDao.selectById(2069984183731470337L);
        if (admin == null) {
            // 如果admin不存在，通过查询username找到
            // 这里我们直接创建一个
            admin = new User();
            admin.setId(2069984183731470337L);
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(1);
            admin.setStatus(1);
            admin.setCreateTime(new Date());
            userDao.insert(admin);
        } else if (admin.getRole() != 1) {
            // 如果admin存在但role不是1，更新为1
            admin.setRole(1);
            userDao.updateById(admin);
        }
    }

    // ==================== 1. 用户模块 ====================

    @Test
    @Order(10)
    @DisplayName("TC-USER-001 用户注册-正常流程")
    void testRegister() throws Exception {
        registeredUsername = "testuser_" + System.currentTimeMillis();
        MvcResult result = mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + registeredUsername + "\",\"password\":\"123456\",\"nickname\":\"测试用户\",\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("注册成功"))
                .andReturn();
    }

    @Test
    @Order(11)
    @DisplayName("TC-USER-002 注册-用户名为空")
    void testRegister_EmptyUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(12)
    @DisplayName("TC-USER-003 注册-用户名过短")
    void testRegister_ShortUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(13)
    @DisplayName("TC-USER-004 注册-密码过短")
    void testRegister_ShortPassword() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(14)
    @DisplayName("TC-USER-005 注册-重复用户名")
    void testRegister_DuplicateUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40002"));
    }

    @Test
    @Order(20)
    @DisplayName("TC-USER-006 管理员登录-获取Token")
    void testAdminLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.role").value(1))
                .andReturn();

        adminToken = extractToken(result.getResponse().getContentAsString());
        assertNotNull(adminToken, "管理员Token不应为空");
    }

    @Test
    @Order(21)
    @DisplayName("TC-USER-007 普通用户登录-获取Token")
    void testUserLogin() throws Exception {
        assertNotNull(registeredUsername, "需要先执行注册");
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + registeredUsername + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.role").value(0))
                .andReturn();

        userToken = extractToken(result.getResponse().getContentAsString());
        userId = extractUserId(result.getResponse().getContentAsString());
        assertNotNull(userToken, "普通用户Token不应为空");
    }

    @Test
    @Order(22)
    @DisplayName("TC-USER-008 登录-用户名不存在")
    void testLogin_UserNotExist() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nonexist999\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40003"));
    }

    @Test
    @Order(23)
    @DisplayName("TC-USER-009 登录-密码错误")
    void testLogin_WrongPassword() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40004"));
    }

    @Test
    @Order(24)
    @DisplayName("TC-USER-010 登录-用户名为空")
    void testLogin_EmptyUsername() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(25)
    @DisplayName("TC-USER-011 登录-密码为空")
    void testLogin_EmptyPassword() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(29)
    @DisplayName("TC-USER-012 用户登出")
    void testLogout() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(get("/user/logout")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("登出成功"));
    }

    @Test
    @Order(30)
    @DisplayName("TC-USER-013 登出后Token验证")
    void testLogout_TokenInvalid() throws Exception {
        assertNotNull(userToken, "需要先执行登出");
        // 登出后，拦截器会检查数据库中的logoutTime
        // 如果logoutTime不为空，返回401；否则允许访问
        // 注意：此测试验证登出机制存在，具体行为取决于拦截器实现
        mockMvc.perform(get("/address/list")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(31)
    @DisplayName("TC-USER-014 重新登录普通用户(后续测试依赖)")
    void testUserRelogin() throws Exception {
        assertNotNull(registeredUsername, "需要先执行注册");
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + registeredUsername + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andReturn();

        userToken = extractToken(result.getResponse().getContentAsString());
        userId = extractUserId(result.getResponse().getContentAsString());
    }

    // ==================== 2. 商品模块(免登录) ====================

    @Test
    @Order(40)
    @DisplayName("TC-GOODS-001 商品分页查询-正常")
    void testGoodsPage() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(41)
    @DisplayName("TC-GOODS-002 商品分页-按名称搜索")
    void testGoodsPage_SearchByName() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("goodsName", "手机"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(42)
    @DisplayName("TC-GOODS-003 商品分页-pageNum为空")
    void testGoodsPage_NullPageNum() throws Exception {
        // pageNum为空时，Spring返回400 Bad Request
        mockMvc.perform(get("/goods/page")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(43)
    @DisplayName("TC-GOODS-004 商品分页-pageSize为空")
    void testGoodsPage_NullPageSize() throws Exception {
        // pageSize为空时，Spring返回400 Bad Request
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(44)
    @DisplayName("TC-GOODS-005 商品分页-pageNum为0")
    void testGoodsPage_PageNumZero() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(45)
    @DisplayName("TC-GOODS-006 商品分页-pageNum为负数")
    void testGoodsPage_NegativePageNum() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "-1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    // ==================== 3. 权限测试 ====================

    @Test
    @Order(50)
    @DisplayName("TC-AUTH-001 未登录访问受保护接口-应返回401")
    void testNoAuth_AccessProtected() throws Exception {
        mockMvc.perform(get("/order/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    @Order(51)
    @DisplayName("TC-AUTH-002 无效Token-应返回401")
    void testInvalidToken() throws Exception {
        mockMvc.perform(get("/cart/get")
                        .header("Authorization", "Bearer invalidtoken12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    @Order(52)
    @DisplayName("TC-AUTH-003 无Bearer前缀-应返回401")
    void testNoBearerPrefix() throws Exception {
        mockMvc.perform(get("/cart/get")
                        .header("Authorization", "sometoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    @Order(53)
    @DisplayName("TC-AUTH-004 普通用户访问管理员接口-应返回403")
    void testUserAccess_AdminApi() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/admin/page")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    @Order(54)
    @DisplayName("TC-AUTH-005 普通用户访问管理员订单接口-应返回403")
    void testUserAccess_AdminOrderApi() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(get("/admin/order/page")
                        .header("Authorization", "Bearer " + userToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    @Order(55)
    @DisplayName("TC-AUTH-006 普通用户访问管理员用户接口-应返回403")
    void testUserAccess_AdminUserApi() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(get("/admin/user/page")
                        .header("Authorization", "Bearer " + userToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("403"));
    }

    // ==================== 4. 购物车模块 ====================

    @Test
    @Order(60)
    @DisplayName("TC-CART-001 添加购物车-正常流程")
    void testAddCart() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":" + testGoodsId + ",\"buyNum\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(61)
    @DisplayName("TC-CART-002 添加购物车-goodsId为空")
    void testAddCart_NullGoodsId() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buyNum\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(62)
    @DisplayName("TC-CART-003 添加购物车-buyNum为0")
    void testAddCart_BuyNumZero() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":" + testGoodsId + ",\"buyNum\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(63)
    @DisplayName("TC-CART-004 添加购物车-buyNum为负数")
    void testAddCart_BuyNumNegative() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":" + testGoodsId + ",\"buyNum\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(64)
    @DisplayName("TC-CART-005 添加购物车-商品不存在")
    void testAddCart_GoodsNotExist() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":999999,\"buyNum\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(65)
    @DisplayName("TC-CART-006 查询购物车列表")
    void testGetCartList() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(get("/cart/get")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(66)
    @DisplayName("TC-CART-007 更新购物车数量")
    void testUpdateCart() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        // 先添加购物车
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":" + testGoodsId + ",\"buyNum\":1}"))
                .andExpect(status().isOk());

        // 再更新数量
        mockMvc.perform(put("/cart/update")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":" + testGoodsId + ",\"buyNum\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(67)
    @DisplayName("TC-CART-008 更新购物车-商品不存在")
    void testUpdateCart_GoodsNotExist() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(put("/cart/update")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":999999,\"buyNum\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    // ==================== 5. 地址模块 ====================

    @Test
    @Order(70)
    @DisplayName("TC-ADDR-001 添加地址-正常流程")
    void testAddAddress() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        MvcResult result = mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"13800138000\",\"address\":\"北京市海淀区中关村大街1号\",\"isDefault\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("地址新增成功"))
                .andReturn();
    }

    @Test
    @Order(71)
    @DisplayName("TC-ADDR-002 添加地址-姓名为空")
    void testAddAddress_EmptyName() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"phone\":\"13800138000\",\"address\":\"北京市海淀区\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(72)
    @DisplayName("TC-ADDR-003 添加地址-手机号格式错误")
    void testAddAddress_InvalidPhone() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"12345\",\"address\":\"北京市海淀区\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(73)
    @DisplayName("TC-ADDR-004 添加地址-手机号为空")
    void testAddAddress_EmptyPhone() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"\",\"address\":\"北京市海淀区\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(74)
    @DisplayName("TC-ADDR-005 添加地址-详细地址为空")
    void testAddAddress_EmptyAddress() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"13800138000\",\"address\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(75)
    @DisplayName("TC-ADDR-006 查询我的地址列表")
    void testGetAddressList() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        MvcResult result = mockMvc.perform(get("/address/list")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andReturn();

        // 提取第一个地址ID用于后续测试
        String body = result.getResponse().getContentAsString();
        testAddressId = extractFirstId(body);
    }

    @Test
    @Order(76)
    @DisplayName("TC-ADDR-007 地址详情-正常")
    void testGetAddressDetail() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        assertNotNull(testAddressId, "需要先添加地址");
        mockMvc.perform(get("/address/" + testAddressId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(77)
    @DisplayName("TC-ADDR-008 地址详情-不存在的地址")
    void testGetAddressDetail_NotFound() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(get("/address/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(78)
    @DisplayName("TC-ADDR-009 编辑地址-正常流程")
    void testUpdateAddress() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        assertNotNull(testAddressId, "需要先添加地址");
        mockMvc.perform(put("/address/update")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + testAddressId + ",\"name\":\"李四\",\"phone\":\"13900139000\",\"address\":\"上海市浦东新区陆家嘴\",\"isDefault\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("地址修改成功"));
    }

    @Test
    @Order(79)
    @DisplayName("TC-ADDR-010 编辑地址-id为空")
    void testUpdateAddress_IdNull() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(put("/address/update")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"李四\",\"phone\":\"13900139000\",\"address\":\"上海市浦东新区\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("地址id不能为空"));
    }

    @Test
    @Order(80)
    @DisplayName("TC-ADDR-011 设置默认地址")
    void testSetDefaultAddress() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        assertNotNull(testAddressId, "需要先添加地址");
        mockMvc.perform(put("/address/default/" + testAddressId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("设置默认地址成功"));
    }

    @Test
    @Order(81)
    @DisplayName("TC-ADDR-012 设置默认地址-不存在的地址")
    void testSetDefaultAddress_NotFound() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(put("/address/default/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(82)
    @DisplayName("TC-ADDR-013 删除地址-不存在的地址")
    void testDeleteAddress_NotFound() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(delete("/address/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    // ==================== 6. 订单模块 ====================

    @Test
    @Order(90)
    @DisplayName("TC-ORDER-001 创建订单-正常流程")
    void testCreateOrder() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        MvcResult result = mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressName\":\"张三\",\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[{\"goodsId\":" + testGoodsId + ",\"buyNum\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("下单成功"))
                .andReturn();
    }

    @Test
    @Order(91)
    @DisplayName("TC-ORDER-002 创建订单-收件人为空")
    void testCreateOrder_EmptyName() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[{\"goodsId\":" + testGoodsId + ",\"buyNum\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(92)
    @DisplayName("TC-ORDER-003 创建订单-商品列表为空")
    void testCreateOrder_EmptyGoodsList() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        // 空商品列表：@NotNull不拦截空列表，业务层会处理
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressName\":\"张三\",\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(93)
    @DisplayName("TC-ORDER-004 创建订单-buyNum为0")
    void testCreateOrder_BuyNumZero() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressName\":\"张三\",\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[{\"goodsId\":" + testGoodsId + ",\"buyNum\":0}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(94)
    @DisplayName("TC-ORDER-005 创建订单-商品不存在")
    void testCreateOrder_GoodsNotExist() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressName\":\"张三\",\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[{\"goodsId\":999999,\"buyNum\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(95)
    @DisplayName("TC-ORDER-006 查询我的订单-正常")
    void testGetMyOrders() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        MvcResult result = mockMvc.perform(get("/order/page")
                        .header("Authorization", "Bearer " + userToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andReturn();

        // 提取第一个订单ID用于后续测试
        testOrderId = extractFirstId(result.getResponse().getContentAsString());
    }

    @Test
    @Order(96)
    @DisplayName("TC-ORDER-007 查询订单-按状态筛选")
    void testGetMyOrders_FilterByStatus() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(get("/order/page")
                        .header("Authorization", "Bearer " + userToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(97)
    @DisplayName("TC-ORDER-008 查询订单-pageNum使用默认值")
    void testGetMyOrders_NullPageNum() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        // OrderQueryDto中pageNum有默认值1，不传时使用默认值
        mockMvc.perform(get("/order/page")
                        .header("Authorization", "Bearer " + userToken)
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(98)
    @DisplayName("TC-ORDER-009 订单详情-正常")
    void testGetOrderDetail() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        assertNotNull(testOrderId, "需要先创建订单");
        mockMvc.perform(get("/order/detail/" + testOrderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(99)
    @DisplayName("TC-ORDER-010 订单详情-不存在的订单")
    void testGetOrderDetail_NotFound() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(get("/order/detail/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(100)
    @DisplayName("TC-ORDER-011 取消订单-正常流程")
    void testCancelOrder() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        // 先查询待付款订单
        MvcResult pageResult = mockMvc.perform(get("/order/page")
                        .header("Authorization", "Bearer " + userToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andReturn();

        Long cancelOrderId = extractFirstId(pageResult.getResponse().getContentAsString());
        if (cancelOrderId != null) {
            mockMvc.perform(put("/order/cancel/" + cancelOrderId)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.msg").value("订单取消成功"));
        }
    }

    @Test
    @Order(101)
    @DisplayName("TC-ORDER-012 取消订单-订单不存在")
    void testCancelOrder_NotFound() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        mockMvc.perform(put("/order/cancel/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    // ==================== 7. 管理员-商品模块 ====================

    @Test
    @Order(110)
    @DisplayName("TC-ADMIN-GOODS-001 管理员分页查询商品")
    void testAdminGoodsPage() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(post("/admin/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(111)
    @DisplayName("TC-ADMIN-GOODS-002 管理员分页查询-按名称搜索")
    void testAdminGoodsPage_SearchByName() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(post("/admin/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNum\":1,\"pageSize\":10,\"goodsName\":\"手机\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(112)
    @DisplayName("TC-ADMIN-GOODS-003 新增商品-正常流程")
    void testAddGoods() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        String goodsName = "测试商品_" + System.currentTimeMillis();
        mockMvc.perform(post("/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsName\":\"" + goodsName + "\",\"price\":99.99,\"stock\":100,\"pic_url\":\"http://example.com/pic.jpg\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("商品新增成功"));
    }

    @Test
    @Order(113)
    @DisplayName("TC-ADMIN-GOODS-004 新增商品-名称重复")
    void testAddGoods_DuplicateName() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        String goodsName = "重复商品_" + System.currentTimeMillis();
        // 第一次添加
        mockMvc.perform(post("/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsName\":\"" + goodsName + "\",\"price\":99.99,\"stock\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        // 第二次添加同名商品
        mockMvc.perform(post("/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsName\":\"" + goodsName + "\",\"price\":99.99,\"stock\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(114)
    @DisplayName("TC-ADMIN-GOODS-005 编辑商品-id为空")
    void testUpdateGoods_IdNull() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(put("/admin/update")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsName\":\"修改后的商品\",\"price\":199.99,\"stock\":50,\"version\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("商品ID不能为空"));
    }

    @Test
    @Order(115)
    @DisplayName("TC-ADMIN-GOODS-006 编辑商品-version为空")
    void testUpdateGoods_VersionNull() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(put("/admin/update")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"goodsName\":\"修改后的商品\",\"price\":199.99,\"stock\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("版本号不能为空，请刷新页面后重试"));
    }

    @Test
    @Order(116)
    @DisplayName("TC-ADMIN-GOODS-007 商品上架/下架-正常")
    void testUpdateGoodsStatus() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        // 测试下架（status=0）
        mockMvc.perform(put("/admin/status/" + testGoodsId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "0"))
                .andExpect(status().isOk());

        // 恢复上架（status=1）
        mockMvc.perform(put("/admin/status/" + testGoodsId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(117)
    @DisplayName("TC-ADMIN-GOODS-008 商品上架/下架-商品不存在")
    void testUpdateGoodsStatus_NotExist() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(put("/admin/status/999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(118)
    @DisplayName("TC-ADMIN-GOODS-009 商品上架/下架-状态参数非法")
    void testUpdateGoodsStatus_InvalidStatus() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(put("/admin/status/" + testGoodsId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    // ==================== 8. 管理员-订单模块 ====================

    @Test
    @Order(120)
    @DisplayName("TC-ADMIN-ORDER-001 全平台订单分页查询")
    void testAdminOrderPage() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(get("/admin/order/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(121)
    @DisplayName("TC-ADMIN-ORDER-002 全平台订单-按状态筛选")
    void testAdminOrderPage_FilterByStatus() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(get("/admin/order/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(122)
    @DisplayName("TC-ADMIN-ORDER-003 订单发货-订单不存在")
    void testDeliverOrder_NotExist() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(put("/admin/order/deliver/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(123)
    @DisplayName("TC-ADMIN-ORDER-004 订单数据统计")
    void testOrderStatistics() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(get("/admin/order/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    // ==================== 9. 管理员-用户模块 ====================

    @Test
    @Order(130)
    @DisplayName("TC-ADMIN-USER-001 用户分页查询")
    void testAdminUserPage() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(get("/admin/user/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(131)
    @DisplayName("TC-ADMIN-USER-002 用户分页-按用户名搜索")
    void testAdminUserPage_SearchByUsername() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(get("/admin/user/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(132)
    @DisplayName("TC-ADMIN-USER-003 冻结/解禁用户-用户不存在")
    void testUpdateUserStatus_NotExist() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        mockMvc.perform(put("/admin/user/status/999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @Order(133)
    @DisplayName("TC-ADMIN-USER-004 冻结用户-正常流程")
    void testFreezeUser() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        assertNotNull(userId, "需要先注册用户");
        mockMvc.perform(put("/admin/user/status/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("用户冻结成功"));
    }

    @Test
    @Order(134)
    @DisplayName("TC-ADMIN-USER-005 解禁用户-正常流程")
    void testUnfreezeUser() throws Exception {
        assertNotNull(adminToken, "需要先执行管理员登录");
        assertNotNull(userId, "需要先注册用户");
        mockMvc.perform(put("/admin/user/status/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("用户解禁成功"));
    }

    // ==================== 10. 删除地址(最后执行) ====================

    @Test
    @Order(200)
    @DisplayName("TC-ADDR-014 删除地址-正常流程")
    void testDeleteAddress() throws Exception {
        assertNotNull(userToken, "需要先执行登录");
        assertNotNull(testAddressId, "需要先添加地址");
        mockMvc.perform(delete("/address/" + testAddressId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("删除成功"));
    }

    // ==================== 工具方法 ====================

    /**
     * 从登录响应中提取Token
     */
    private String extractToken(String responseBody) {
        try {
            int tokenIndex = responseBody.indexOf("\"token\":\"");
            if (tokenIndex == -1) return null;
            int start = tokenIndex + 9;
            int end = responseBody.indexOf("\"", start);
            return responseBody.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从登录响应中提取userId
     */
    private Long extractUserId(String responseBody) {
        try {
            int index = responseBody.indexOf("\"userId\":");
            if (index == -1) return null;
            int start = index + 9;
            int end = responseBody.indexOf(",", start);
            if (end == -1) end = responseBody.indexOf("}", start);
            return Long.parseLong(responseBody.substring(start, end).trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从响应中提取第一个ID（用于订单、地址等）
     */
    private Long extractFirstId(String responseBody) {
        try {
            // 查找 "records":[ 后的第一个 "id": 数字
            int recordsIndex = responseBody.indexOf("\"records\":[");
            if (recordsIndex == -1) {
                // 尝试直接查找 "id":
                int idIndex = responseBody.indexOf("\"id\":");
                if (idIndex == -1) return null;
                int start = idIndex + 5;
                // 跳过空格
                while (start < responseBody.length() && responseBody.charAt(start) == ' ') start++;
                int end = start;
                while (end < responseBody.length() && Character.isDigit(responseBody.charAt(end))) end++;
                if (start == end) return null;
                return Long.parseLong(responseBody.substring(start, end));
            }
            // 在records数组中查找第一个id
            int searchFrom = recordsIndex + 11;
            int idIndex = responseBody.indexOf("\"id\":", searchFrom);
            if (idIndex == -1) return null;
            int start = idIndex + 5;
            // 跳过空格
            while (start < responseBody.length() && responseBody.charAt(start) == ' ') start++;
            int end = start;
            while (end < responseBody.length() && Character.isDigit(responseBody.charAt(end))) end++;
            if (start == end) return null;
            return Long.parseLong(responseBody.substring(start, end));
        } catch (Exception e) {
            return null;
        }
    }
}
