package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.dto.request.LoginRequest;
import com.yaren.careerpilot.dto.request.RegisterRequest;
import com.yaren.careerpilot.dto.response.AuthResponse;
import com.yaren.careerpilot.dto.response.UserInfoDto;
import com.yaren.careerpilot.entity.User;
import com.yaren.careerpilot.exception.EmailAlreadyExistsException;
import com.yaren.careerpilot.repository.UserRepository;
import com.yaren.careerpilot.security.JwtUtil;
import com.yaren.careerpilot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use.");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        UserInfoDto userInfo = new UserInfoDto(
                user.getId().toString(),
                user.getFullName(),
                user.getEmail()
        );

        return new AuthResponse(token, userInfo);
    }
    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getEmail());

        // Kullanıcıyı veritabanından çek (bilgilerini frontend'e yollamak için)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfoDto userInfo = new UserInfoDto(
                user.getId().toString(),
                user.getFullName(),
                user.getEmail()
        );

        return new AuthResponse(token, userInfo);
    }

    @Override
    public UserInfoDto updateProfile(String email, String newName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(newName);
        user = userRepository.save(user);
        return new UserInfoDto(user.getId().toString(), user.getFullName(), user.getEmail());
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.yaren.careerpilot.service.FileStorageService fileStorageService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.yaren.careerpilot.repository.ResumeRepository resumeRepository;

    @Override
    @jakarta.transaction.Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Fİziksel PDF dosyalarını sil
        if (user.getResumes() != null) {
            for (com.yaren.careerpilot.entity.Resume resume : user.getResumes()) {
                if (resume.getFilePath() != null) {
                    try {
                        fileStorageService.delete(resume.getFilePath());
                    } catch (Exception e) {
                        // Log and ignore to ensure user deletion continues
                    }
                }
            }
            // Hibernate'in FK hatası vermemesi için CV'leri manuel ve öncelikli siliyoruz.
            resumeRepository.deleteAll(user.getResumes());
            user.getResumes().clear();
        }
        
        // Veritabanından kullanıcıyı sil
        userRepository.delete(user);
    }
}
