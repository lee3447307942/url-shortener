package com.shorturl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shorturl.entity.ClickLog;
import com.shorturl.entity.ShortUrl;
import com.shorturl.mapper.ClickLogMapper;
import com.shorturl.mapper.ShortUrlMapper;
import com.shorturl.service.ShortUrlService;
import com.shorturl.util.ShortCodeGenerator;
import com.shorturl.util.UserAgentParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ShortUrlServiceImpl implements ShortUrlService {

    private final ShortUrlMapper shortUrlMapper;
    private final ClickLogMapper clickLogMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_PREFIX = "short:";

    @Override
    public ShortUrl createShortUrl(String originalUrl, String customCode, Integer expireDays, Long userId) {
        String shortCode = (customCode != null && !customCode.isEmpty()) ? customCode : ShortCodeGenerator.generate();

        // check duplicate
        if (customCode != null) {
            Long count = shortUrlMapper.selectCount(
                new LambdaQueryWrapper<ShortUrl>().eq(ShortUrl::getShortCode, shortCode));
            if (count > 0) {
                throw new RuntimeException("短码已存在: " + shortCode);
            }
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setUserId(userId);
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setClickCount(0L);
        if (expireDays != null && expireDays > 0) {
            shortUrl.setExpireAt(LocalDateTime.now().plusDays(expireDays));
        }
        shortUrlMapper.insert(shortUrl);

        // cache in redis
        redisTemplate.opsForValue().set(REDIS_PREFIX + shortCode, originalUrl, 24, TimeUnit.HOURS);

        return shortUrl;
    }

    @Override
    public String getOriginalUrl(String shortCode) {
        // try redis first
        String cached = redisTemplate.opsForValue().get(REDIS_PREFIX + shortCode);
        if (cached != null) {
            return cached;
        }

        ShortUrl shortUrl = shortUrlMapper.selectOne(
            new LambdaQueryWrapper<ShortUrl>().eq(ShortUrl::getShortCode, shortCode));
        if (shortUrl == null) {
            return null;
        }
        if (shortUrl.getExpireAt() != null && shortUrl.getExpireAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        // cache and return
        redisTemplate.opsForValue().set(REDIS_PREFIX + shortCode, shortUrl.getOriginalUrl(), 24, TimeUnit.HOURS);
        return shortUrl.getOriginalUrl();
    }

    @Override
    public Map<String, Object> getStats(String shortCode) {
        ShortUrl shortUrl = shortUrlMapper.selectOne(
            new LambdaQueryWrapper<ShortUrl>().eq(ShortUrl::getShortCode, shortCode));
        if (shortUrl == null) {
            return null;
        }

        List<ClickLog> logs = clickLogMapper.selectList(
            new LambdaQueryWrapper<ClickLog>()
                .eq(ClickLog::getShortCode, shortCode)
                .orderByDesc(ClickLog::getClickedAt)
                .last("LIMIT 100"));

        Map<String, Object> result = new HashMap<>();
        result.put("shortCode", shortUrl.getShortCode());
        result.put("originalUrl", shortUrl.getOriginalUrl());
        result.put("clickCount", shortUrl.getClickCount());
        result.put("createdAt", shortUrl.getCreatedAt());
        result.put("expireAt", shortUrl.getExpireAt());
        result.put("recentClicks", logs);
        return result;
    }

    @Override
    public List<ShortUrl> getMyLinks(Long userId) {
        return shortUrlMapper.selectList(
            new LambdaQueryWrapper<ShortUrl>()
                .eq(ShortUrl::getUserId, userId)
                .orderByDesc(ShortUrl::getCreatedAt));
    }

    public void recordClick(String shortCode, String ip, String userAgent, String referer) {
        // update count
        shortUrlMapper.update(null,
            new LambdaUpdateWrapper<ShortUrl>()
                .eq(ShortUrl::getShortCode, shortCode)
                .setSql("click_count = click_count + 1"));

        // save log
        ClickLog log = new ClickLog();
        log.setShortCode(shortCode);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setReferer(referer);
        log.setBrowser(UserAgentParser.parseBrowser(userAgent));
        log.setOs(UserAgentParser.parseOS(userAgent));
        log.setClickedAt(LocalDateTime.now());
        clickLogMapper.insert(log);
    }
}
