package com.xiaoyu;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单管理系统 全模块集成测试
 *
 * 覆盖: 用户认证、商品、购物车、订单、地址 五大模块
 * 测试类型: 功能测试 + 边界异常测试 + 权限测试
 *
 * 运行: mvn test -Dtest=OrderSystemFullTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("订单管理系统 - 全模块集成测试")
public class OrderSystemFullTest {

    @Autowired
    private MockMvc mockMvc;

    /** 管理员Token (role=1) */
    private static String adminToken;
    /** 普通用户Token (role=0) */
    private static String userToken;

    // ==================== 用户模块测试 ====================

    @Test
    @Order(1)
    @DisplayName("TC-USER-007 管理员登录-获取Token")
    void testAdminLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.role").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // 提取token用于后续测试
        adminToken = extractToken(body);
        assertNotNull(adminToken, "管理员Token不应为空");
    }

    @Test
    @Order(2)
    @DisplayName("TC-USER-001 用户注册-正常流程")
    void testRegister() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test_" + System.currentTimeMillis() + "\",\"password\":\"123456\",\"nickname\":\"测试用户\",\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("注册成功"));
    }

    @Test
    @Order(3)
    @DisplayName("TC-USER-002 注册-用户名为空")
    void testRegister_EmptyUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(4)
    @DisplayName("TC-USER-003 注册-用户名过短")
    void testRegister_ShortUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(5)
    @DisplayName("TC-USER-005 注册-密码过短")
    void testRegister_ShortPassword() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(6)
    @DisplayName("TC-USER-006 注册-重复用户名")
    void testRegister_DuplicateUsername() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40002"));
    }

    @Test
    @Order(7)
    @DisplayName("TC-USER-008 登录-用户名不存在")
    void testLogin_UserNotExist() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nonexist999\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40003"));
    }

    @Test
    @Order(8)
    @DisplayName("TC-USER-009 登录-密码错误")
    void testLogin_WrongPassword() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40004"));
    }

    @Test
    @Order(9)
    @DisplayName("TC-USER-010 登录-用户名为空")
    void testLogin_EmptyUsername() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(10)
    @DisplayName("TC-USER-011 用户登出")
    void testLogout() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(get("/user/logout")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("登出成功"));
    }

    // ==================== 商品模块测试(免登录) ====================

    @Test
    @Order(20)
    @DisplayName("TC-GOODS-001 商品分页查询-正常")
    void testGoodsPage() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(21)
    @DisplayName("TC-GOODS-003 商品分页-pageNum为空")
    void testGoodsPage_NullPageNum() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(22)
    @DisplayName("TC-GOODS-004 商品分页-pageNum为0")
    void testGoodsPage_PageNumZero() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(23)
    @DisplayName("TC-GOODS-005 商品分页-pageNum为负数")
    void testGoodsPage_NegativePageNum() throws Exception {
        mockMvc.perform(get("/goods/page")
                        .param("pageNum", "-1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    // ==================== 权限测试 ====================

    @Test
    @Order(30)
    @DisplayName("TC-API-004 未登录访问受保护接口-应返回401")
    void testNoAuth_AccessProtected() throws Exception {
        mockMvc.perform(get("/order/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    @Order(31)
    @DisplayName("TC-ADMIN-008 非管理员访问管理接口-应返回403")
    void testUserAccess_AdminApi() throws Exception {
        // 先用普通用户登录
        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();
        if (!loginBody.contains("\"code\":\"200\"")) {
            // 如果普通用户不存在，跳过此测试
            return;
        }
        String normalToken = extractToken(loginBody);
        if (normalToken == null) return;

        // 用普通用户Token访问管理接口
        mockMvc.perform(post("/admin/page")
                        .header("Authorization", "Bearer " + normalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    @Order(32)
    @DisplayName("TC-API-003 无效Token-应返回401")
    void testInvalidToken() throws Exception {
        mockMvc.perform(get("/cart/get")
                        .header("Authorization", "Bearer invalidtoken12345")
                        .param("goodsId", "1")
                        .param("buyNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("401"));
    }

    // ==================== 购物车模块测试 ====================

    @Test
    @Order(40)
    @DisplayName("TC-CART-001 添加购物车-正常流程")
    void testAddCart() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":1,\"buyNum\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(41)
    @DisplayName("TC-CART-002 添加购物车-goodsId为空")
    void testAddCart_NullGoodsId() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buyNum\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(42)
    @DisplayName("TC-CART-003 添加购物车-buyNum为0")
    void testAddCart_BuyNumZero() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":1,\"buyNum\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(43)
    @DisplayName("TC-CART-004 添加购物车-buyNum为负数")
    void testAddCart_BuyNumNegative() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/cart/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":1,\"buyNum\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(44)
    @DisplayName("TC-CART-005 查询购物车")
    void testGetCart() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(get("/cart/get")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("goodsId", "1")
                        .param("buyNum", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(45)
    @DisplayName("TC-CART-006 更新购物车数量")
    void testUpdateCart() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(put("/cart/update")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goodsId\":1,\"buyNum\":5}"))
                .andExpect(status().isOk());
    }

    // ==================== 地址模块测试 ====================

    @Test
    @Order(50)
    @DisplayName("TC-ADDR-001 添加地址-正常流程")
    void testAddAddress() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"13800138000\",\"address\":\"北京市海淀区中关村大街1号\",\"isDefault\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(51)
    @DisplayName("TC-ADDR-002 添加地址-姓名为空")
    void testAddAddress_EmptyName() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"phone\":\"13800138000\",\"address\":\"北京市海淀区\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(52)
    @DisplayName("TC-ADDR-003 添加地址-手机号格式错误")
    void testAddAddress_InvalidPhone() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"12345\",\"address\":\"北京市海淀区\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(53)
    @DisplayName("TC-ADDR-004 添加地址-手机号为空")
    void testAddAddress_EmptyPhone() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"\",\"address\":\"北京市海淀区\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(54)
    @DisplayName("TC-ADDR-005 添加地址-详细地址为空")
    void testAddAddress_EmptyAddress() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"phone\":\"13800138000\",\"address\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(55)
    @DisplayName("TC-ADDR-006 查询我的地址列表")
    void testGetAddressList() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(get("/address/list")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    // ==================== 订单模块测试 ====================

    @Test
    @Order(60)
    @DisplayName("TC-ORDER-001 创建订单-正常流程")
    void testCreateOrder() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressName\":\"张三\",\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[{\"goodsId\":1,\"buyNum\":2}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(61)
    @DisplayName("TC-ORDER-002 创建订单-收件人为空")
    void testCreateOrder_EmptyName() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[{\"goodsId\":1,\"buyNum\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(62)
    @DisplayName("TC-ORDER-003 创建订单-商品列表为空")
    void testCreateOrder_EmptyGoodsList() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressName\":\"张三\",\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(63)
    @DisplayName("TC-ORDER-004 创建订单-buyNum为0")
    void testCreateOrder_BuyNumZero() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(post("/order/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressName\":\"张三\",\"addressPhone\":\"13800138000\",\"addressDetail\":\"北京市海淀区\",\"goodsList\":[{\"goodsId\":1,\"buyNum\":0}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(64)
    @DisplayName("TC-ORDER-005 查询我的订单")
    void testGetMyOrders() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(get("/order/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(65)
    @DisplayName("TC-ORDER-006 查询订单-按状态筛选")
    void testGetMyOrders_FilterByStatus() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(get("/order/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @Order(66)
    @DisplayName("TC-ORDER-007 查询订单-pageNum为空")
    void testGetMyOrders_NullPageNum() throws Exception {
        assertNotNull(adminToken, "需要先执行登录");
        mockMvc.perform(get("/order/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("40001"));
    }

    @Test
    @Order(67)
    @DisplayName("TC-ORDER-012 未登录访问订单-应返回401")
    void testOrder_NoAuth() throws Exception {
        mockMvc.perform(get("/order/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("401"));
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
}
