package com.onepiece.xmz.app.domain.repository;

import com.onepiece.xmz.app.domain.entity.TaskExecutionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务执行记录Repository
 * 
 * @author SmartAI-Assistant
 */
@Repository
public interface TaskExecutionRecordRepository extends JpaRepository<TaskExecutionRecord, Long> {
    
    /**
     * 根据任务ID查找
     */
    Optional<TaskExecutionRecord> findByTaskId(String taskId);
    
    /**
     * 根据状态查找任务
     */
    List<TaskExecutionRecord> findByStatus(String status);
    
    /**
     * 根据智能体ID查找任务
     */
    List<TaskExecutionRecord> findByAgentIdAndStatus(Long agentId, String status);
    
    /**
     * 分页查询任务历史
     */
    Page<TaskExecutionRecord> findByOrderByCreateTimeDesc(Pageable pageable);
    
    /**
     * 根据任务类型查找
     */
    List<TaskExecutionRecord> findByTaskTypeAndStatus(String taskType, String status);
    
    /**
     * 查找指定时间范围内的任务
     */
    @Query("SELECT t FROM TaskExecutionRecord t " +
           "WHERE t.createTime >= :startTime AND t.createTime <= :endTime " +
           "ORDER BY t.createTime DESC")
    List<TaskExecutionRecord> findTasksInTimeRange(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计各状态任务数量
     */
    @Query("SELECT t.status, COUNT(t) FROM TaskExecutionRecord t GROUP BY t.status")
    List<Object[]> countTasksByStatus();
    
    /**
     * 统计今日任务数量
     */
    @Query("SELECT COUNT(t) FROM TaskExecutionRecord t " +
           "WHERE DATE(t.createTime) = CURRENT_DATE AND t.status = :status")
    Long countTodayTasksByStatus(@Param("status") String status);
    
    /**
     * 计算平均执行时间
     */
    @Query("SELECT AVG(t.executionTime) FROM TaskExecutionRecord t " +
           "WHERE t.status = 'COMPLETED' AND t.executionTime IS NOT NULL")
    Double getAverageExecutionTime();
    
    /**
     * 查找运行中的任务
     */
    @Query("SELECT t FROM TaskExecutionRecord t " +
           "WHERE t.status IN ('PENDING', 'RUNNING') " +
           "ORDER BY t.createTime ASC")
    List<TaskExecutionRecord> findActiveTasks();
    
    /**
     * 查找最近完成的任务
     */
    @Query("SELECT t FROM TaskExecutionRecord t " +
           "WHERE t.status = 'COMPLETED' " +
           "ORDER BY t.endTime DESC")
    List<TaskExecutionRecord> findRecentCompletedTasks(Pageable pageable);
}


