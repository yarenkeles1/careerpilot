package com.yaren.careerpilot.controller;

import com.yaren.careerpilot.dto.request.LoginRequest;
import com.yaren.careerpilot.dto.request.RegisterRequest;
import com.yaren.careerpilot.dto.response.AuthResponse;
import com.yaren.careerpilot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@jakarta.validation.Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PutMapping("/profile")
    public ResponseEntity<com.yaren.careerpilot.dto.response.UserInfoDto> updateProfile(@RequestBody java.util.Map<String, String> body, java.security.Principal principal) {
        return ResponseEntity.ok(authService.updateProfile(principal.getName(), body.get("name")));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteProfile(java.security.Principal principal) {
        authService.deleteAccount(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
