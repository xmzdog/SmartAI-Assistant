package com.onepiece.xmz.app.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.springframework.jdbc.core.JdbcTemplate;


import javax.sql.DataSource;


@Configuration
public class DataSourceConfig {

    @Bean("mysqlDataSource")
    @Primary
    public DataSource mysqlDataSource(@Value("${spring.datasource.mysql.driver-class-name}") String driverClassName,
                                        @Value("${spring.datasource.mysql.url}") String url,
                                        @Value("${spring.datasource.mysql.username}") String username,
                                        @Value("${spring.datasource.mysql.password}") String password,
                                        @Value("${spring.datasource.mysql.hikari.maximum-pool-size:20}") int maximumPoolSize,
                                        @Value("${spring.datasource.mysql.hikari.minimum-idle:5}") int minimumIdle,
                                        @Value("${spring.datasource.mysql.hikari.idle-timeout:600000}") long idleTimeout,
                                        @Value("${spring.datasource.mysql.hikari.connection-timeout:30000}") long connectionTimeout,
                                        @Value("${spring.datasource.mysql.hikari.max-lifetime:1800000}") long maxLifetime,
                                        @Value("${spring.datasource.mysql.hikari.pool-name:MainHikariPool}") String poolName,
                                        @Value("${spring.datasource.mysql.hikari.connection-test-query:SELECT 1}") String connectionTestQuery,
                                        @Value("${spring.datasource.mysql.hikari.auto-commit:true}") boolean autoCommit,
                                        @Value("${spring.datasource.mysql.hikari.leak-detection-threshold:60000}") long leakDetectionThreshold,
                                        @Value("${spring.datasource.mysql.hikari.validation-timeout:5000}") long validationTimeout) {
        // MySQL 连接池配置
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 连接池基本配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setPoolName(poolName);
        
        // 连接验证配置
        dataSource.setConnectionTestQuery(connectionTestQuery);
        dataSource.setValidationTimeout(validationTimeout);
        
        // 性能优化配置
        dataSource.setAutoCommit(autoCommit);
        dataSource.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // MySQL 特定优化
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.addDataSourceProperty("useServerPrepStmts", "true");
        dataSource.addDataSourceProperty("useLocalSessionState", "true");
        dataSource.addDataSourceProperty("rewriteBatchedStatements", "true");
        dataSource.addDataSourceProperty("cacheResultSetMetadata", "true");
        dataSource.addDataSourceProperty("cacheServerConfiguration", "true");
        dataSource.addDataSourceProperty("elideSetAutoCommits", "true");
        dataSource.addDataSourceProperty("maintainTimeStats", "false");

        return dataSource;
    }



    @Bean("pgVectorDataSource")
    public DataSource pgVectorDataSource(@Value("${spring.datasource.pgvector.driver-class-name}") String driverClassName,
                                         @Value("${spring.datasource.pgvector.url}") String url,
                                         @Value("${spring.datasource.pgvector.username}") String username,
                                         @Value("${spring.datasource.pgvector.password}") String password,
                                         @Value("${spring.datasource.pgvector.hikari.maximum-pool-size:8}") int maximumPoolSize,
                                         @Value("${spring.datasource.pgvector.hikari.minimum-idle:2}") int minimumIdle,
                                         @Value("${spring.datasource.pgvector.hikari.idle-timeout:300000}") long idleTimeout,
                                         @Value("${spring.datasource.pgvector.hikari.connection-timeout:30000}") long connectionTimeout,
                                         @Value("${spring.datasource.pgvector.hikari.max-lifetime:1800000}") long maxLifetime,
                                         @Value("${spring.datasource.pgvector.hikari.pool-name:PgVectorHikariPool}") String poolName,
                                         @Value("${spring.datasource.pgvector.hikari.connection-test-query:SELECT 1}") String connectionTestQuery,
                                         @Value("${spring.datasource.pgvector.hikari.leak-detection-threshold:60000}") long leakDetectionThreshold,
                                         @Value("${spring.datasource.pgvector.hikari.validation-timeout:5000}") long validationTimeout) {
        // PgVector 连接池配置
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 连接池基本配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setPoolName(poolName);
        
        // 连接验证配置
        dataSource.setConnectionTestQuery(connectionTestQuery);
        dataSource.setValidationTimeout(validationTimeout);
        
        // 性能优化配置
        dataSource.setAutoCommit(true);
        dataSource.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // PostgreSQL 特定优化
        dataSource.addDataSourceProperty("prepareThreshold", "3");
        dataSource.addDataSourceProperty("preparedStatementCacheQueries", "256");
        dataSource.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
        dataSource.addDataSourceProperty("defaultRowFetchSize", "1000");
        dataSource.addDataSourceProperty("logUnclosedConnections", "true");
        dataSource.addDataSourceProperty("tcpKeepAlive", "true");
        dataSource.addDataSourceProperty("ApplicationName", "SmartAI-PgVector");
        
        // 启动时连接失败处理
        dataSource.setInitializationFailTimeout(1);

        return dataSource;
    }

    @Bean("pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    /**
     * MySQL 数据源健康检查
     */
    @Bean("mysqlHealthIndicator")
    public HealthIndicator mysqlHealthIndicator(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) {
        return () -> {
            try {
                HikariDataSource hikariDataSource = (HikariDataSource) mysqlDataSource;
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
                
                return Health.up()
                        .withDetail("database", "MySQL")
                        .withDetail("poolName", hikariDataSource.getPoolName())
                        .withDetail("active", poolBean.getActiveConnections())
                        .withDetail("idle", poolBean.getIdleConnections())
                        .withDetail("total", poolBean.getTotalConnections())
                        .withDetail("awaiting", poolBean.getThreadsAwaitingConnection())
                        .withDetail("maxPoolSize", hikariDataSource.getMaximumPoolSize())
                        .withDetail("minIdle", hikariDataSource.getMinimumIdle())
                        .build();
            } catch (Exception ex) {
                return Health.down()
                        .withDetail("database", "MySQL")
                        .withDetail("error", ex.getMessage())
                        .build();
            }
        };
    }
    
    /**
     * PgVector 数据源健康检查
     */
    @Bean("pgVectorHealthIndicator")
    public HealthIndicator pgVectorHealthIndicator(@Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        return () -> {
            try {
                HikariDataSource hikariDataSource = (HikariDataSource) pgVectorDataSource;
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
                
                return Health.up()
                        .withDetail("database", "PostgreSQL-PgVector")
                        .withDetail("poolName", hikariDataSource.getPoolName())
                        .withDetail("active", poolBean.getActiveConnections())
                        .withDetail("idle", poolBean.getIdleConnections())
                        .withDetail("total", poolBean.getTotalConnections())
                        .withDetail("awaiting", poolBean.getThreadsAwaitingConnection())
                        .withDetail("maxPoolSize", hikariDataSource.getMaximumPoolSize())
                        .withDetail("minIdle", hikariDataSource.getMinimumIdle())
                        .build();
            } catch (Exception ex) {
                return Health.down()
                        .withDetail("database", "PostgreSQL-PgVector")
                        .withDetail("error", ex.getMessage())
                        .build();
            }
        };
    }

}
