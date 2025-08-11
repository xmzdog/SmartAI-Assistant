package com.onepiece.xmz.app.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客户端顾问配置实体
 * 
 * @author SmartAI-Assistant
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ai_client_advisor")
public class AiClientAdvisor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "advisor_name", nullable = false, length = 50)
    private String advisorName;
    
    @Column(name = "advisor_type", nullable = false, length = 50)
    private String advisorType;
    
    @Column(name = "order_num")
    private Integer orderNum = 0;
    
    @Column(name = "ext_param", length = 2048)
    private String extParam;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 注释掉关联关系，使用简单的ID引用
    // @OneToMany(mappedBy = "advisor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<AiClientAdvisorConfig> advisorConfigs;
    
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
