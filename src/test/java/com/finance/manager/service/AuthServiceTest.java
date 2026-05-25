package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.User;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ConflictException;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        User saved = User.builder().id(1L).username("test@test.com").build();
        when(userRepository.save(any())).thenReturn(saved);

        RegisterResponse res = authService.register(registerRequest);

        assertEquals("User registered successfully", res.getMessage());
        assertEquals(1L, res.getUserId());
    }

    @Test
    void register_DuplicateUsername_ThrowsConflict() {
        when(userRepository.existsByUsername("test@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("test@test.com");
        req.setPassword("password123");

        Authentication auth = new UsernamePasswordAuthenticationToken("test@test.com", null, Collections.emptyList());
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(httpReq.getSession(true)).thenReturn(session);

        MessageResponse res = authService.login(req, httpReq);
        assertEquals("Login successful", res.getMessage());
    }

    @Test
    void login_BadCredentials_ThrowsBadRequest() {
        LoginRequest req = new LoginRequest();
        req.setUsername("test@test.com");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        assertThrows(BadRequestException.class, () -> authService.login(req, httpReq));
    }

    @Test
    void logout_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);

        MessageResponse res = authService.logout(request);
        assertEquals("Logout successful", res.getMessage());
        verify(session).invalidate();
    }

    @Test
    void logout_NoSession_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        MessageResponse res = authService.logout(request);
        assertEquals("Logout successful", res.getMessage());
    }
}
