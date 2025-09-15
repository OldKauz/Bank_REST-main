package com.example.bankcards.controller;

import com.example.bankcards.dto.UserRegistrationDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success() {
        User user = new User("john", "encodedPass", "USER");
        user.setId(1L);

        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(user);

        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setUsername("john");
        dto.setPassword("123456");
        dto.setRole("USER");

        ResponseEntity<?> response = authController.register(dto);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void login_success() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setUsername("john");
        dto.setPassword("123456");

        User user = new User("john", "encodedPass", "USER");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("john", "USER")).thenReturn("mockToken");

        ResponseEntity<?> response = authController.login(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("token"));
    }
}
