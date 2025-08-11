package com.onepiece.xmz.app.domain.repository;

import com.onepiece.xmz.app.domain.entity.SystemMonitorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 系统监控数据Repository
 * 
 * @author SmartAI-Assistant
 */
@Repository
public interface SystemMonitorDataRepository extends JpaRepository<SystemMonitorData, Long> {
    
    /**
     * 获取最新的监控数据
     */
    Optional<SystemMonitorData> findTopByOrderByMonitorTimeDesc();
    
    /**
     * 获取指定时间范围内的监控数据
     */
    @Query("SELECT s FROM SystemMonitorData s " +
           "WHERE s.monitorTime >= :startTime AND s.monitorTime <= :endTime " +
           "ORDER BY s.monitorTime DESC")
    List<SystemMonitorData> findByTimeRange(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取最近N条监控数据
     */
    List<SystemMonitorData> findTop10ByOrderByMonitorTimeDesc();
    
    /**
     * 获取最近24小时的监控数据
     */
    @Query("SELECT s FROM SystemMonitorData s " +
           "WHERE s.monitorTime >= :yesterday " +
           "ORDER BY s.monitorTime DESC")
    List<SystemMonitorData> findLast24Hours(@Param("yesterday") LocalDateTime yesterday);
    
    /**
     * 获取平均系统负载
     */
    @Query("SELECT AVG(s.systemLoad) FROM SystemMonitorData s " +
           "WHERE s.monitorTime >= :startTime")
    Double getAverageSystemLoad(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取平均CPU使用率
     */
    @Query("SELECT AVG(s.cpuUsage) FROM SystemMonitorData s " +
           "WHERE s.monitorTime >= :startTime")
    Double getAverageCpuUsage(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取平均内存使用率
     */
    @Query("SELECT AVG(s.memoryUsage) FROM SystemMonitorData s " +
           "WHERE s.monitorTime >= :startTime")
    Double getAverageMemoryUsage(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 删除过期的监控数据（保留最近30天）
     */
    @Query("DELETE FROM SystemMonitorData s WHERE s.monitorTime < :cutoffTime")
    void deleteOldData(@Param("cutoffTime") LocalDateTime cutoffTime);
}


