package com.onepiece.xmz.app.domain.repository;

import com.onepiece.xmz.app.domain.entity.TaskExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务执行日志Repository
 * 
 * @author SmartAI-Assistant
 */
@Repository
public interface TaskExecutionLogRepository extends JpaRepository<TaskExecutionLog, Long> {
    
    /**
     * 根据任务记录ID查询执行日志，按步骤顺序排序
     */
    List<TaskExecutionLog> findByTaskRecordIdOrderByStepOrderAsc(Long taskRecordId);
    
    /**
     * 统计指定任务记录的日志条数
     */
    int countByTaskRecordId(Long taskRecordId);
    
    /**
     * 根据任务记录ID和步骤状态查询日志
     */
    List<TaskExecutionLog> findByTaskRecordIdAndStepStatus(Long taskRecordId, String stepStatus);
    
    /**
     * 根据步骤类型查询日志
     */
    List<TaskExecutionLog> findByStepType(String stepType);
    
    /**
     * 查询指定时间范围内的日志
     */
    @Query("SELECT log FROM TaskExecutionLog log WHERE log.createTime BETWEEN :startTime AND :endTime ORDER BY log.createTime DESC")
    List<TaskExecutionLog> findLogsInTimeRange(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);
    
    /**
     * 删除指定时间之前的日志
     */
    @Query("DELETE FROM TaskExecutionLog log WHERE log.createTime < :cutoffTime")
    void deleteOldLogs(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 根据任务记录ID删除所有相关日志
     */
    void deleteByTaskRecordId(Long taskRecordId);
    
    /**
     * 查询最近的执行日志
     */
    List<TaskExecutionLog> findTop10ByOrderByCreateTimeDesc();
    
    /**
     * 统计失败的步骤数量
     */
    @Query("SELECT COUNT(log) FROM TaskExecutionLog log WHERE log.stepStatus = 'FAILED' AND log.createTime >= :startTime")
    Long countFailedStepsToday(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 统计成功的步骤数量
     */
    @Query("SELECT COUNT(log) FROM TaskExecutionLog log WHERE log.stepStatus = 'SUCCESS' AND log.createTime >= :startTime")
    Long countSuccessStepsToday(@Param("startTime") LocalDateTime startTime);
}


