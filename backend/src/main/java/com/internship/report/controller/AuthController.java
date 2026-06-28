package com.internship.report.controller;

import com.internship.report.dto.*;
import com.internship.report.service.RefreshTokenService;
import com.internship.report.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = userService.login(request);
        // v2.1 生成并设置 Refresh Token
        String refreshToken = refreshTokenService.createRefreshToken(response.getId());
        response.setRefreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtResponse>> register(@Valid @RequestBody RegisterRequest request) {
        JwtResponse response = userService.register(request);
        String refreshToken = refreshTokenService.createRefreshToken(response.getId());
        response.setRefreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("注册成功", response));
    }

    /**
     * v2.1 Refresh Token 无感刷新
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        String newToken = refreshTokenService.refreshAccessToken(request.getRefreshToken());
        JwtResponse response = new JwtResponse();
        response.setToken(newToken);
        // 生成新的 Refresh Token（轮换策略）
        Long userId = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        if (userId != null) {
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
            response.setRefreshToken(refreshTokenService.createRefreshToken(userId));
        }
        return ResponseEntity.ok(ApiResponse.success("刷新成功", response));
    }
}
