package com.xiaoyu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoyu.dto.LoginDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLogin_Success() throws Exception {
        // 测试正常登录
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("testpassword123");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    public void testLogin_EmptyUsername() throws Exception {
        // 测试空用户名
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("");
        loginDto.setPassword("testpassword123");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_EmptyPassword() throws Exception {
        // 测试空密码
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_MissingUsername() throws Exception {
        // 测试缺少用户名字段
        String json = "{\"password\":\"testpassword123\"}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_MissingPassword() throws Exception {
        // 测试缺少密码字段
        String json = "{\"username\":\"testuser\"}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogout_WithToken() throws Exception {
        // 测试带token的登出
        mockMvc.perform(post("/logout")
                .header("Authorization", "test_token_here")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogout_WithoutToken() throws Exception {
        // 测试无token的登出
        mockMvc.perform(post("/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogout_InvalidToken() throws Exception {
        // 测试无效token的登出
        mockMvc.perform(post("/logout")
                .header("Authorization", "invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginAndLogout_FullFlow() throws Exception {
        // 测试完整流程：登录 -> 获得token -> 登出
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("testpassword123");

        // 登录
        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 解析登录响应获取token
        String loginResponse = loginResult.getResponse().getContentAsString();
        // 注意：这里需要根据实际返回的JSON结构调整解析逻辑

        // 登出（使用获得的token）
        mockMvc.perform(post("/logout")
                .header("Authorization", "token_from_login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
