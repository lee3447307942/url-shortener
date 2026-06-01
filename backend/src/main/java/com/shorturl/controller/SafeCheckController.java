package com.shorturl.controller;

import com.shorturl.service.SafeCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SafeCheckController {

    private final SafeCheckService safeCheckService;

    @GetMapping("/api/check-safe")
    public ResponseEntity<Map<String, Object>> checkSafe(@RequestParam String url) {
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(safeCheckService.checkUrl(url));
    }
}
