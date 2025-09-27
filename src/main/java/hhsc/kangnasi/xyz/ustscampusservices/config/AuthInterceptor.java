package hhsc.kangnasi.xyz.ustscampusservices.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final RedissonClient redissonClient;

    public AuthInterceptor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return unauthorized(response, "未登录");
        }

        String token = extractToken(authHeader);
        if (token == null || token.isBlank()) {
            return unauthorized(response, "未登录");
        }

        String key = "auth:token:" + token;
        RBucket<String> bucket = redissonClient.getBucket(key);
        String email = bucket.get();
        if (email == null || email.isBlank()) {
            return unauthorized(response, "未登录或Token无效");
        }

        // Attach resolved principal for downstream use
        request.setAttribute("X-User-Email", email);
        return true;
    }

    private static String extractToken(String authHeader) {
        String prefix = "Bearer ";
        if (authHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return authHeader.substring(prefix.length()).trim();
        }
        return authHeader.trim();
    }

    private static boolean unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":401,\"message\":\"" + escapeJson(message) + "\"}";
        response.getWriter().write(body);
        return false;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

