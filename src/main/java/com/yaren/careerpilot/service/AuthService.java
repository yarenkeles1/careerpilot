package com.yaren.careerpilot.service;

import com.yaren.careerpilot.dto.request.LoginRequest;
import com.yaren.careerpilot.dto.request.RegisterRequest;
import com.yaren.careerpilot.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    com.yaren.careerpilot.dto.response.UserInfoDto updateProfile(String email, String newName);

    void deleteAccount(String email);
}
