package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.dto.request.LoginRequest;
import com.yaren.careerpilot.dto.request.RegisterRequest;
import com.yaren.careerpilot.dto.response.AuthResponse;
import com.yaren.careerpilot.entity.User;
import com.yaren.careerpilot.exception.EmailAlreadyExistsException;
import com.yaren.careerpilot.repository.UserRepository;
import com.yaren.careerpilot.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthServiceImpl authService;
    @Test
    void register_newEmail_returnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Yaren Keles");
        request.setEmail("yaren@test.com");
        request.setPassword("123456");
        when(userRepository.existsByEmail("yaren@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken("yaren@test.com")).thenReturn("mock.jwt.token");
        AuthResponse response = authService.register(request);
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
    }
    @Test
    void register_duplicateEmail_throwsEmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Yaren Keles");
        request.setEmail("yaren@test.com");
        request.setPassword("123456");
        when(userRepository.existsByEmail("yaren@test.com")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("already in use");
    }
    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("yaren@test.com");
        request.setPassword("123456");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(jwtUtil.generateToken("yaren@test.com")).thenReturn("mock.jwt.token");
        AuthResponse response = authService.login(request);
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
    }
    @Test
    void login_wrongPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("yaren@test.com");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
