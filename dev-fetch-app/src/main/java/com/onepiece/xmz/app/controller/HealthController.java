package com.onepiece.xmz.app.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于验证数据源配置是否正常工作
 * 
 * @author SmartAI-Assistant
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private final DataSource mysqlDataSource;
    private final DataSource pgVectorDataSource;
    
    public HealthController(
            @Qualifier("mysqlDataSource") DataSource mysqlDataSource,
            @Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        this.mysqlDataSource = mysqlDataSource;
        this.pgVectorDataSource = pgVectorDataSource;
    }
    
    /**
     * 检查数据源连接状态
     */
    @GetMapping("/datasources")
    public ResponseEntity<Map<String, Object>> checkDataSources() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查 MySQL 连接
        try (Connection connection = mysqlDataSource.getConnection()) {
            result.put("mysql", Map.of(
                "status", "UP",
                "url", connection.getMetaData().getURL(),
                "valid", connection.isValid(5)
            ));
        } catch (Exception e) {
            result.put("mysql", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
        
        // 检查 PgVector 连接
        try (Connection connection = pgVectorDataSource.getConnection()) {
            result.put("pgvector", Map.of(
                "status", "UP",
                "url", connection.getMetaData().getURL(),
                "valid", connection.isValid(5)
            ));
        } catch (Exception e) {
            result.put("pgvector", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
        
        return ResponseEntity.ok(result);
    }
}

