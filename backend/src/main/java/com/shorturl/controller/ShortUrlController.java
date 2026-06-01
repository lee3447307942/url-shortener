package com.shorturl.controller;

import com.shorturl.entity.ShortUrl;
import com.shorturl.service.impl.ShortUrlServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlServiceImpl shortUrlService;

    @Value("${short-url.base-url}")
    private String baseUrl;

    @PostMapping("/api/shorten")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
                                                       @RequestAttribute Long userId) {
        String url = (String) body.get("url");
        String customCode = (String) body.get("customCode");
        Integer expireDays = body.get("expireDays") != null ? ((Number) body.get("expireDays")).intValue() : null;

        if (url == null || url.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "URL不能为空");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            ShortUrl result = shortUrlService.createShortUrl(url, customCode, expireDays, userId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("shortUrl", baseUrl + "/" + result.getShortCode());
            resp.put("shortCode", result.getShortCode());
            resp.put("originalUrl", result.getOriginalUrl());
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage() != null ? e.getMessage() : "未知错误");
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/api/batch-shorten")
    public ResponseEntity<Map<String, Object>> batchCreate(@RequestBody Map<String, Object> body,
                                                            @RequestAttribute Long userId) {
        List<String> urls = (List<String>) body.get("urls");
        if (urls == null || urls.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "URL列表不能为空");
            return ResponseEntity.badRequest().body(err);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            if (url == null || url.trim().isEmpty()) continue;
            try {
                ShortUrl result = shortUrlService.createShortUrl(url.trim(), null, null, userId);
                Map<String, Object> item = new HashMap<>();
                item.put("originalUrl", url.trim());
                item.put("shortUrl", baseUrl + "/" + result.getShortCode());
                item.put("shortCode", result.getShortCode());
                results.add(item);
            } catch (Exception e) {
                errors.add("第" + (i + 1) + "条失败: " + e.getMessage());
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", results);
        resp.put("errors", errors);
        resp.put("total", results.size());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/my/links")
    public ResponseEntity<Map<String, Object>> myLinks(@RequestAttribute Long userId) {
        List<ShortUrl> links = shortUrlService.getMyLinks(userId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("links", links);
        resp.put("total", links.size());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String originalUrl = shortUrlService.getOriginalUrl(shortCode);
        if (originalUrl == null) {
            return ResponseEntity.notFound().build();
        }

        // async record click
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        try {
            shortUrlService.recordClick(shortCode, ip, ua, referer);
        } catch (Exception ignored) {}

        return ResponseEntity.status(302).header("Location", originalUrl).build();
    }

    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<Map<String, Object>> stats(@PathVariable String shortCode) {
        Map<String, Object> stats = shortUrlService.getStats(shortCode);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }
}
