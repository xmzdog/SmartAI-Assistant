package com.onepiece.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天内容配置
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "chat.content")
public class ChatContentConfig {

    /**
     * 内容大小阈值（字节），超过此大小将使用大内容处理
     * 默认: 64KB
     */
    private int sizeThreshold = 64 * 1024;

    /**
     * 是否启用内容压缩
     * 默认: true
     */
    private boolean compressionEnabled = true;

    /**
     * 压缩算法类型
     * 支持: gzip, lz4, snappy
     * 默认: gzip
     */
    private String compressionType = "gzip";

    /**
     * 是否启用内容去重
     * 默认: true
     */
    private boolean deduplicationEnabled = true;

    /**
     * 内容块大小（字节）
     * 默认: 1MB
     */
    private int chunkSize = 1024 * 1024;

    /**
     * 是否启用内容缓存
     * 默认: true
     */
    private boolean cacheEnabled = true;

    /**
     * 缓存过期时间（小时）
     * 默认: 24小时
     */
    private int cacheExpirationHours = 24;

    /**
     * 最大缓存大小（MB）
     * 默认: 100MB
     */
    private int maxCacheSizeMB = 100;

    /**
     * 是否记录详细日志
     * 默认: false
     */
    private boolean verboseLogging = false;

}
