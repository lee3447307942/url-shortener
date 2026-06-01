package com.shorturl.service;

import com.shorturl.entity.ShortUrl;
import java.util.List;
import java.util.Map;

public interface ShortUrlService {
    ShortUrl createShortUrl(String originalUrl, String customCode, Integer expireDays, Long userId);
    String getOriginalUrl(String shortCode);
    Map<String, Object> getStats(String shortCode);
    List<ShortUrl> getMyLinks(Long userId);
}
