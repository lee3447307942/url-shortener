package com.shorturl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("short_url")
public class ShortUrl {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String shortCode;
    private String originalUrl;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private Long clickCount;
}
