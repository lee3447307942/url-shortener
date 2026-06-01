package com.shorturl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("click_log")
public class ClickLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String shortCode;
    private String ip;
    private String userAgent;
    private String referer;
    private String browser;
    private String os;
    private LocalDateTime clickedAt;
}
