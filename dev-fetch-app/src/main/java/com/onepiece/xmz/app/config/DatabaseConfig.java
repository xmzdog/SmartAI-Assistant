package com.onepiece.xmz.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

/**
 * 数据库配置
 * 支持多数据源：MySQL(主业务) + PostgreSQL(向量存储)
 * 
 * @author SmartAI-Assistant
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.onepiece.xmz.app.domain.repository",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
@EntityScan(basePackages = "com.onepiece.xmz.app.domain.entity")
@EnableTransactionManagement
public class DatabaseConfig {
    
    /**
     * 手动创建 EntityManagerFactoryBuilder
     * 这是解决多数据源下 EntityManagerFactoryBuilder 缺失的关键
     */
    @Bean
    @Primary
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            JpaVendorAdapter jpaVendorAdapter,
            JpaProperties jpaProperties,
            HibernateProperties hibernateProperties) {
        
        return new EntityManagerFactoryBuilder(
                jpaVendorAdapter,
                jpaProperties.getProperties(),
                null);
    }
    
    /**
     * JPA Vendor Adapter 配置
     */
    @Bean
    @Primary
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
        adapter.setShowSql(true);
        adapter.setGenerateDdl(true);
        return adapter;
    }
    
    /**
     * JPA Properties Bean
     */
    @Bean
    @Primary
    public JpaProperties jpaProperties() {
        JpaProperties properties = new JpaProperties();
        properties.setShowSql(true);
        properties.getProperties().put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.getProperties().put("hibernate.hbm2ddl.auto", "update");
        properties.getProperties().put("hibernate.format_sql", "true");
        return properties;
    }
    
    /**
     * Hibernate Properties Bean
     */
    @Bean
    @Primary
    public HibernateProperties hibernateProperties() {
        return new HibernateProperties();
    }

    /**
     * 主数据源EntityManagerFactory - 用于JPA实体管理
     * 使用 DataSourceConfig 中定义的 mysqlDataSource
     */
    @Primary
    @Bean("primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mysqlDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.onepiece.xmz.app.domain.entity")
                .persistenceUnit("primary")
                .properties(hibernateProperties().determineHibernateProperties(
                        jpaProperties().getProperties(),
                        new HibernateSettings()))
                .build();
    }

    /**
     * 主数据源事务管理器
     */
    @Primary
    @Bean("primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}


