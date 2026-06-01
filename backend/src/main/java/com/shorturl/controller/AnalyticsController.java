package com.shorturl.controller;

import com.shorturl.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/api/analytics/{shortCode}")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> analytics = analyticsService.getAnalytics(shortCode, days);
        if (analytics == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analytics);
    }
}
