package com.costinsight.user.controller;

import com.costinsight.user.dto.ChangePasswordRequest;
import com.costinsight.user.dto.UserResponseVO;
import com.costinsight.user.dto.UserUpdateRequest;
import com.costinsight.user.service.UserService;
import com.costinsight.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil; // Mock JwtUtil because the interceptor depends on it

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUpdateCurrentUser_Success() throws Exception {
        // Given
        Long currentUserId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("new.email@example.com");

        UserResponseVO updatedUserVO = new UserResponseVO();
        updatedUserVO.setId(currentUserId);
        updatedUserVO.setUsername("testuser");
        updatedUserVO.setEmail("new.email@example.com");

        when(userService.updateUserById(eq(currentUserId), any(UserUpdateRequest.class))).thenReturn(updatedUserVO);

        // When & Then
        mockMvc.perform(put("/api/users/me")
                        .requestAttr("userId", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(currentUserId))
                .andExpect(jsonPath("$.data.email").value("new.email@example.com"));
    }

    @Test
    void testGetUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        UserResponseVO userVO = new UserResponseVO();
        userVO.setId(userId);
        userVO.setUsername("testuser");

        when(userService.findUserById(userId)).thenReturn(userVO);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testChangeCurrentUserPassword_Success() throws Exception {
        // Given
        Long currentUserId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmNewPassword("newPass");

        doNothing().when(userService).changePassword(eq(currentUserId), any(ChangePasswordRequest.class));

        // When & Then
        mockMvc.perform(put("/api/users/me/password")
                        .requestAttr("userId", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    void testChangeCurrentUserPassword_Failure() throws Exception {
        // Given
        Long currentUserId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPass");
        request.setNewPassword("newPass");
        request.setConfirmNewPassword("newPass");

        doThrow(new IllegalArgumentException("Invalid old password"))
                .when(userService).changePassword(eq(currentUserId), any(ChangePasswordRequest.class));

        // When & Then
        mockMvc.perform(put("/api/users/me/password")
                        .requestAttr("userId", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid old password"));
    }
}