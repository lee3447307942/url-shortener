package com.shorturl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shorturl.entity.ClickLog;
import com.shorturl.entity.ShortUrl;
import com.shorturl.mapper.ClickLogMapper;
import com.shorturl.mapper.ShortUrlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickLogMapper clickLogMapper;
    private final ShortUrlMapper shortUrlMapper;

    /**
     * 获取短链的详细统计
     */
    public Map<String, Object> getAnalytics(String shortCode, int days) {
        ShortUrl shortUrl = shortUrlMapper.selectOne(
            new LambdaQueryWrapper<ShortUrl>().eq(ShortUrl::getShortCode, shortCode));
        if (shortUrl == null) return null;

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<ClickLog> logs = clickLogMapper.selectList(
            new LambdaQueryWrapper<ClickLog>()
                .eq(ClickLog::getShortCode, shortCode)
                .ge(ClickLog::getClickedAt, since)
                .orderByAsc(ClickLog::getClickedAt));

        Map<String, Object> result = new HashMap<>();
        result.put("shortCode", shortCode);
        result.put("originalUrl", shortUrl.getOriginalUrl());
        result.put("totalClicks", shortUrl.getClickCount());
        result.put("periodClicks", logs.size());

        // 按天统计
        Map<String, Long> dailyClicks = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getClickedAt().toLocalDate().toString(),
                Collectors.counting()));
        result.put("dailyClicks", dailyClicks);

        // 浏览器分布
        Map<String, Long> browsers = logs.stream()
            .filter(log -> log.getBrowser() != null)
            .collect(Collectors.groupingBy(ClickLog::getBrowser, Collectors.counting()));
        result.put("browsers", browsers);

        // 操作系统分布
        Map<String, Long> osList = logs.stream()
            .filter(log -> log.getOs() != null)
            .collect(Collectors.groupingBy(ClickLog::getOs, Collectors.counting()));
        result.put("os", osList);

        // 来源分布
        Map<String, Long> referrers = logs.stream()
            .filter(log -> log.getReferer() != null && !log.getReferer().isEmpty())
            .collect(Collectors.groupingBy(
                log -> extractDomain(log.getReferer()),
                Collectors.counting()));
        result.put("referrers", referrers);

        // 最近点击
        List<ClickLog> recentClicks = logs.stream()
            .sorted((a, b) -> b.getClickedAt().compareTo(a.getClickedAt()))
            .limit(50)
            .collect(Collectors.toList());
        result.put("recentClicks", recentClicks);

        return result;
    }

    private String extractDomain(String url) {
        try {
            if (url.startsWith("http://")) url = url.substring(7);
            else if (url.startsWith("https://")) url = url.substring(8);
            int slash = url.indexOf('/');
            return slash > 0 ? url.substring(0, slash) : url;
        } catch (Exception e) {
            return url;
        }
    }
}
