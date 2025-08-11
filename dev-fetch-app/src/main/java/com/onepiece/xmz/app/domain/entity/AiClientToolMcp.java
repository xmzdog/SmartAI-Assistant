package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客户端MCP工具实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client_tool_mcp")
public class AiClientToolMcp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "mcp_name", nullable = false, length = 50)
    private String mcpName;
    
    @Column(name = "transport_type", nullable = false, length = 20)
    private String transportType;
    
    @Column(name = "transport_config", length = 1024)
    private String transportConfig;
    
    @Column(name = "request_timeout")
    private Integer requestTimeout = 180;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @OneToMany(mappedBy = "mcpTool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<AiClientToolConfig> toolConfigs;
    
    // @OneToMany(mappedBy = "mcpTool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<AiClientModelToolConfig> modelToolConfigs;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
