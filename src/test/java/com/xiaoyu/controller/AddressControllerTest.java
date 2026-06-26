package com.xiaoyu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoyu.Dao.UserDao;
import com.xiaoyu.common.Code;
import com.xiaoyu.common.Result;
import com.xiaoyu.dto.AddressDTO;
import com.xiaoyu.pojo.User;
import com.xiaoyu.service.AddressService;
import com.xiaoyu.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDao userDao;

    private String token;
    private AddressDTO validAddressDTO;

    @BeforeEach
    public void setUp() {
        // 模拟 JWT 认证链路
        token = "valid_jwt_token";
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims();
        claims.put("userId", 1L);
        claims.put("role", 0);

        when(jwtUtil.parseToken(token)).thenReturn(claims);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setLogoutTime(null);
        when(userDao.selectById(1L)).thenReturn(mockUser);

        // 准备有效的地址 DTO
        validAddressDTO = new AddressDTO();
        validAddressDTO.setName("张三");
        validAddressDTO.setPhone("13800138000");
        validAddressDTO.setAddress("北京市朝阳区xxx街道");
        validAddressDTO.setIsDefault(0);
    }

    // ==================== 添加地址 ====================

    @Test
    public void testAddAddress_Success() throws Exception {
        when(addressService.addAddress(any(AddressDTO.class)))
                .thenReturn(new Result(Code.SUCCESS, null, "地址新增成功"));

        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("地址新增成功"));
    }

    @Test
    public void testAddAddress_SetAsDefault() throws Exception {
        validAddressDTO.setIsDefault(1);

        when(addressService.addAddress(any(AddressDTO.class)))
                .thenReturn(new Result(Code.SUCCESS, null, "地址新增成功"));

        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    public void testAddAddress_BlankName() throws Exception {
        validAddressDTO.setName("");

        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddAddress_InvalidPhone() throws Exception {
        validAddressDTO.setPhone("12345");

        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddAddress_BlankAddress() throws Exception {
        validAddressDTO.setAddress("");

        mockMvc.perform(post("/address/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== 编辑地址 ====================

    @Test
    public void testUpdateAddress_Success() throws Exception {
        validAddressDTO.setId(1L);

        when(addressService.updateAddress(any(AddressDTO.class)))
                .thenReturn(new Result(Code.SUCCESS, null, "地址修改成功"));

        mockMvc.perform(put("/address/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("地址修改成功"));
    }

    @Test
    public void testUpdateAddress_IdNull() throws Exception {
        // id 为空时，service 层返回失败
        when(addressService.updateAddress(any(AddressDTO.class)))
                .thenReturn(new Result(Code.FAIL, null, "地址id不能为空"));

        mockMvc.perform(put("/address/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("地址id不能为空"));
    }

    @Test
    public void testUpdateAddress_NotFound() throws Exception {
        validAddressDTO.setId(999L);

        when(addressService.updateAddress(any(AddressDTO.class)))
                .thenReturn(new Result(Code.FAIL, null, "地址不存在"));

        mockMvc.perform(put("/address/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("地址不存在"));
    }

    @Test
    public void testUpdateAddress_BlankName() throws Exception {
        validAddressDTO.setId(1L);
        validAddressDTO.setName("");

        mockMvc.perform(put("/address/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== 删除地址 ====================

    @Test
    public void testDeleteAddress_Success() throws Exception {
        when(addressService.deleteAddress(1L))
                .thenReturn(new Result(Code.SUCCESS, null, "删除成功"));

        mockMvc.perform(delete("/address/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("删除成功"));
    }

    @Test
    public void testDeleteAddress_NotFound() throws Exception {
        when(addressService.deleteAddress(999L))
                .thenReturn(new Result(Code.FAIL, null, "地址不存在或无权限"));

        mockMvc.perform(delete("/address/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("地址不存在或无权限"));
    }

    // ==================== 设置默认地址 ====================

    @Test
    public void testSetDefault_Success() throws Exception {
        when(addressService.setDefault(1L))
                .thenReturn(new Result(Code.SUCCESS, null, "设置默认地址成功"));

        mockMvc.perform(put("/address/default/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("设置默认地址成功"));
    }

    @Test
    public void testSetDefault_NotFound() throws Exception {
        when(addressService.setDefault(999L))
                .thenReturn(new Result(Code.FAIL, null, "地址不存在"));

        mockMvc.perform(put("/address/default/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("地址不存在"));
    }

    // ==================== 地址列表 ====================

    @Test
    public void testGetMyAddressList_Success() throws Exception {
        Map<String, Object> addr1 = new HashMap<>();
        addr1.put("id", 1L);
        addr1.put("name", "张三");
        addr1.put("phone", "13800138000");
        addr1.put("address", "北京市朝阳区");
        addr1.put("isDefault", 1);

        Map<String, Object> addr2 = new HashMap<>();
        addr2.put("id", 2L);
        addr2.put("name", "李四");
        addr2.put("phone", "13900139000");
        addr2.put("address", "上海市浦东新区");
        addr2.put("isDefault", 0);

        when(addressService.getMyAddressList())
                .thenReturn(new Result(Code.SUCCESS, Arrays.asList(addr1, addr2), "查询成功"));

        mockMvc.perform(get("/address/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("查询成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("张三"))
                .andExpect(jsonPath("$.data[0].isDefault").value(1))
                .andExpect(jsonPath("$.data[1].name").value("李四"));
    }

    @Test
    public void testGetMyAddressList_Empty() throws Exception {
        when(addressService.getMyAddressList())
                .thenReturn(new Result(Code.SUCCESS, Collections.emptyList(), "查询成功"));

        mockMvc.perform(get("/address/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== 地址详情 ====================

    @Test
    public void testGetAddressById_Success() throws Exception {
        Map<String, Object> addr = new HashMap<>();
        addr.put("id", 1L);
        addr.put("userId", 1L);
        addr.put("name", "张三");
        addr.put("phone", "13800138000");
        addr.put("address", "北京市朝阳区xxx街道");
        addr.put("isDefault", 0);

        when(addressService.getAddressById(1L))
                .thenReturn(new Result(Code.SUCCESS, addr, "查询成功"));

        mockMvc.perform(get("/address/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("查询成功"))
                .andExpect(jsonPath("$.data.name").value("张三"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.address").value("北京市朝阳区xxx街道"));
    }

    @Test
    public void testGetAddressById_NotFound() throws Exception {
        when(addressService.getAddressById(999L))
                .thenReturn(new Result(Code.FAIL, null, "地址不存在"));

        mockMvc.perform(get("/address/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("地址不存在"));
    }

    // ==================== 未登录测试 ====================

    @Test
    public void testAddAddress_NoToken() throws Exception {
        // 不传 Authorization 头，拦截器返回 401 JSON（HTTP 状态码仍为 200）
        mockMvc.perform(post("/address/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("未登录，请登录"));
    }
}
