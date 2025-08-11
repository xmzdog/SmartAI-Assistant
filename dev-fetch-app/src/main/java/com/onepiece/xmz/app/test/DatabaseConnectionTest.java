package com.onepiece.xmz.app.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 数据库连接测试
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;

    @Autowired
    @Qualifier("pgVectorDataSource")
    private DataSource pgVectorDataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 开始测试数据库连接 ===");
        
        // 测试主数据源 (MySQL)
        testMySQLConnection();
        
        // 测试向量数据源 (PostgreSQL)
        testPgVectorConnection();
        
        log.info("=== 数据库连接测试完成 ===");
    }
    
    private void testMySQLConnection() {
        try (Connection connection = mysqlDataSource.getConnection()) {
            log.info("✓ MySQL 数据源连接成功: {}", connection.getMetaData().getURL());
            
            // 执行简单查询测试
            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 as test_value");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    log.info("✓ MySQL 查询测试成功: test_value = {}", rs.getInt("test_value"));
                }
            }
            
            // 测试数据库版本
            try (PreparedStatement stmt = connection.prepareStatement("SELECT VERSION() as version");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    log.info("✓ MySQL 版本: {}", rs.getString("version"));
                }
            }
            
        } catch (Exception e) {
            log.error("✗ MySQL 数据源连接失败: {}", e.getMessage(), e);
        }
    }
    
    private void testPgVectorConnection() {
        try (Connection connection = pgVectorDataSource.getConnection()) {
            log.info("✓ PostgreSQL 数据源连接成功: {}", connection.getMetaData().getURL());
            
            // 执行简单查询测试
            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 as test_value");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    log.info("✓ PostgreSQL 查询测试成功: test_value = {}", rs.getInt("test_value"));
                }
            }
            
        } catch (Exception e) {
            log.error("✗ PostgreSQL 数据源连接失败: {}", e.getMessage(), e);
        }
    }
}
