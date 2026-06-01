package com.shorturl.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SafeCheckService {

    // 常见被封域名/关键词
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "tiktok.com", "facebook.com", "twitter.com", "instagram.com",
        "youtube.com", "google.com", "telegram.org", "whatsapp.com"
    );

    private static final Set<String> SUSPICIOUS_TLDS = Set.of(
        ".xyz", ".top", ".club", ".wang", ".vip", ".work"
    );

    /**
     * 检测URL是否可能被封
     * 返回: {safe: true/false, reason: "...", risk: "low/medium/high"}
     */
    public Map<String, Object> checkUrl(String url) {
        Map<String, Object> result = new HashMap<>();
        String lowerUrl = url.toLowerCase();

        // 检查是否是已知被封域名
        for (String domain : BLOCKED_DOMAINS) {
            if (lowerUrl.contains(domain)) {
                result.put("safe", false);
                result.put("risk", "high");
                result.put("reason", "目标域名在国内通常被屏蔽: " + domain);
                return result;
            }
        }

        // 检查可疑TLD
        for (String tld : SUSPICIOUS_TLDS) {
            if (lowerUrl.contains(tld)) {
                result.put("safe", true);
                result.put("risk", "medium");
                result.put("reason", "使用了常见被风控的顶级域名: " + tld);
                return result;
            }
        }

        // 检查是否是IP地址
        String domain = extractDomain(lowerUrl);
        if (domain != null && domain.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            result.put("safe", true);
            result.put("risk", "medium");
            result.put("reason", "使用IP地址而非域名，可能被风控");
            return result;
        }

        // 检查URL长度和特殊字符
        if (lowerUrl.length() > 500) {
            result.put("safe", true);
            result.put("risk", "low");
            result.put("reason", "URL较长，建议缩短");
            return result;
        }

        result.put("safe", true);
        result.put("risk", "low");
        result.put("reason", "未检测到风险");
        return result;
    }

    private String extractDomain(String url) {
        try {
            if (url.startsWith("http://")) url = url.substring(7);
            else if (url.startsWith("https://")) url = url.substring(8);
            int slash = url.indexOf('/');
            return slash > 0 ? url.substring(0, slash) : url;
        } catch (Exception e) {
            return null;
        }
    }
}
