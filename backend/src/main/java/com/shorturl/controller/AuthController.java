package com.shorturl.controller;

import com.shorturl.entity.User;
import com.shorturl.service.UserService;
import com.shorturl.util.JwtUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 50, message = "用户名长度2-50")
        private String username;
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度6-100")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @PostMapping("/api/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest req) {
        try {
            User user = userService.register(req.getUsername(), req.getEmail(), req.getPassword());
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("user", user);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage() != null ? e.getMessage() : "注册失败，请稍后重试");
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        try {
            User user = userService.login(req.getEmail(), req.getPassword());
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("user", user);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage() != null ? e.getMessage() : "登录失败，请稍后重试");
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> me(@RequestAttribute Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("user", user);
        return ResponseEntity.ok(resp);
    }

    @Data
    public static class DomainRequest {
        private String customDomain;
    }

    @PutMapping("/api/me/domain")
    public ResponseEntity<Map<String, Object>> updateDomain(@RequestAttribute Long userId,
                                                             @RequestBody DomainRequest req) {
        try {
            userService.updateDomain(userId, req.getCustomDomain());
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "域名设置成功");
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
