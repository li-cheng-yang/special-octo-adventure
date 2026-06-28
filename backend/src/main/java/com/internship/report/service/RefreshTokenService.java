package com.internship.report.service;

import com.internship.report.entity.User;
import com.internship.report.repository.UserRepository;
import com.internship.report.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    // 内存存储 Refresh Token → userId，生产环境建议使用Redis
    private final Map<String, RefreshTokenEntry> tokenStore = new ConcurrentHashMap<>();

    private static final long REFRESH_TOKEN_EXPIRE_MILLIS = 7L * 24 * 60 * 60 * 1000; // 7天

    public String createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, new RefreshTokenEntry(userId, System.currentTimeMillis()));
        return token;
    }

    public Long validateRefreshToken(String token) {
        RefreshTokenEntry entry = tokenStore.get(token);
        if (entry == null) return null;
        // 检查是否过期
        if (System.currentTimeMillis() - entry.createTime > REFRESH_TOKEN_EXPIRE_MILLIS) {
            tokenStore.remove(token);
            return null;
        }
        return entry.userId;
    }

    public void revokeRefreshToken(String token) {
        tokenStore.remove(token);
    }

    public String refreshAccessToken(String refreshToken) {
        Long userId = validateRefreshToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Refresh Token 无效或已过期");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return jwtUtils.generateToken(userId, user.getUsername(), user.getRole().name());
    }

    /**
     * 定期清理过期token（可由定时任务调用）
     */
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(e -> now - e.getValue().createTime > REFRESH_TOKEN_EXPIRE_MILLIS);
    }

    private record RefreshTokenEntry(Long userId, long createTime) {}
}
