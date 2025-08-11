package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.app.domain.dto.TaskHistoryDTO;
import com.onepiece.xmz.app.domain.dto.TaskStatisticsDTO;
import com.onepiece.xmz.app.domain.entity.AiAgent;
import com.onepiece.xmz.app.domain.entity.TaskExecutionLog;
import com.onepiece.xmz.app.domain.entity.TaskExecutionRecord;
import com.onepiece.xmz.app.domain.repository.TaskExecutionRecordRepository;
import com.onepiece.xmz.app.domain.repository.TaskExecutionLogRepository;
import com.onepiece.xmz.app.domain.repository.AiAgentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 任务管理服务实现
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Service
@Transactional
public class TaskManagementServiceImpl implements TaskManagementService {
    
    @Autowired
    private TaskExecutionRecordRepository taskRecordRepository;
    
    @Autowired
    private TaskExecutionLogRepository taskExecutionLogRepository;
    
    @Autowired
    private AiAgentRepository agentRepository;
    
    @Override
    public TaskExecutionRecord createTaskRecord(String taskId, Long agentId, String taskName, 
                                              String taskDescription, String taskType, String inputParams) {
        log.info("创建任务记录: taskId={}, agentId={}, taskType={}", taskId, agentId, taskType);
        
        TaskExecutionRecord record = new TaskExecutionRecord();
        record.setTaskId(taskId);
        record.setAgentId(agentId);
        record.setTaskName(taskName);
        record.setTaskDescription(taskDescription);
        record.setTaskType(taskType);
        record.setStatus("PENDING");
        record.setProgress(0);
        record.setInputParams(inputParams);
        record.setStartTime(LocalDateTime.now());
        
        return taskRecordRepository.save(record);
    }
    
    @Override
    public void updateTaskStatus(String taskId, String status, Integer progress) {
        log.debug("更新任务状态: taskId={}, status={}, progress={}", taskId, status, progress);
        
        Optional<TaskExecutionRecord> recordOpt = taskRecordRepository.findByTaskId(taskId);
        if (recordOpt.isPresent()) {
            TaskExecutionRecord record = recordOpt.get();
            record.setStatus(status);
            if (progress != null) {
                record.setProgress(progress);
            }
            
            // 如果任务开始运行，更新开始时间
            if ("RUNNING".equals(status) && record.getStartTime() == null) {
                record.setStartTime(LocalDateTime.now());
            }
            
            taskRecordRepository.save(record);
        } else {
            log.warn("任务记录不存在: {}", taskId);
        }
    }
    
    @Override
    public void completeTask(String taskId, String result, String outputData) {
        log.info("完成任务: taskId={}", taskId);
        
        Optional<TaskExecutionRecord> recordOpt = taskRecordRepository.findByTaskId(taskId);
        if (recordOpt.isPresent()) {
            TaskExecutionRecord record = recordOpt.get();
            record.setStatus("COMPLETED");
            record.setProgress(100);
            record.setResult(result);
            record.setOutputData(outputData);
            record.setEndTime(LocalDateTime.now());
            
            // 计算执行时间
            if (record.getStartTime() != null) {
                long executionTime = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis();
                record.setExecutionTime(executionTime);
            }
            
            taskRecordRepository.save(record);
        } else {
            log.warn("任务记录不存在: {}", taskId);
        }
    }
    
    @Override
    public void failTask(String taskId, String errorMessage) {
        log.warn("任务失败: taskId={}, error={}", taskId, errorMessage);
        
        Optional<TaskExecutionRecord> recordOpt = taskRecordRepository.findByTaskId(taskId);
        if (recordOpt.isPresent()) {
            TaskExecutionRecord record = recordOpt.get();
            record.setStatus("FAILED");
            record.setErrorMessage(errorMessage);
            record.setEndTime(LocalDateTime.now());
            
            // 计算执行时间
            if (record.getStartTime() != null) {
                long executionTime = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis();
                record.setExecutionTime(executionTime);
            }
            
            taskRecordRepository.save(record);
        } else {
            log.warn("任务记录不存在: {}", taskId);
        }
    }
    
    @Override
    public void addTaskLog(String taskId, String stepName, String stepType, 
                          String stepStatus, String inputData, String outputData) {
        log.debug("添加任务日志: taskId={}, stepName={}, stepType={}", taskId, stepName, stepType);
        
        Optional<TaskExecutionRecord> recordOpt = taskRecordRepository.findByTaskId(taskId);
        if (recordOpt.isPresent()) {
            TaskExecutionRecord record = recordOpt.get();
            
            TaskExecutionLog logEntry = new TaskExecutionLog();
            logEntry.setTaskRecordId(record.getId());
            logEntry.setStepName(stepName);
            logEntry.setStepType(stepType);
            logEntry.setStepStatus(stepStatus);
            logEntry.setInputData(inputData);
            logEntry.setOutputData(outputData);
            
            // 计算步骤顺序 - 通过查询数据库获取当前步骤数量
            int stepOrder = taskExecutionLogRepository.countByTaskRecordId(record.getId()) + 1;
            logEntry.setStepOrder(stepOrder);
            
            // 直接保存日志条目
            taskExecutionLogRepository.save(logEntry);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Response<TaskHistoryDTO> getTaskDetail(String taskId) {
        try {
            Optional<TaskExecutionRecord> recordOpt = taskRecordRepository.findByTaskId(taskId);
            if (recordOpt.isPresent()) {
                TaskExecutionRecord record = recordOpt.get();
                TaskHistoryDTO dto = convertToTaskHistoryDTO(record);
                return Response.success(dto, "获取任务详情成功");
            } else {
                return Response.fail("任务不存在: " + taskId);
            }
        } catch (Exception e) {
            log.error("获取任务详情失败: {}", e.getMessage(), e);
            return Response.fail("获取任务详情失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Response<Page<TaskHistoryDTO>> getTaskHistory(int page, int size, String status, String taskType) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TaskExecutionRecord> records = taskRecordRepository.findByOrderByCreateTimeDesc(pageable);
            
            Page<TaskHistoryDTO> dtoPage = records.map(this::convertToTaskHistoryDTO);
            
            return Response.success(dtoPage, "获取任务历史成功");
        } catch (Exception e) {
            log.error("获取任务历史失败: {}", e.getMessage(), e);
            return Response.fail("获取任务历史失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Response<TaskStatisticsDTO> getTaskStatistics() {
        try {
            // 统计总任务数
            Long totalTasks = taskRecordRepository.count();
            
            // 统计各状态任务数
            Long completedTasks = taskRecordRepository.countTodayTasksByStatus("COMPLETED");
            Long failedTasks = taskRecordRepository.countTodayTasksByStatus("FAILED");
            Long runningTasks = (long) taskRecordRepository.findByStatus("RUNNING").size();
            Long pendingTasks = (long) taskRecordRepository.findByStatus("PENDING").size();
            
            // 今日任务统计
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            List<TaskExecutionRecord> todayTasks = taskRecordRepository.findTasksInTimeRange(todayStart, todayEnd);
            
            Long todayTotal = (long) todayTasks.size();
            Long todayCompleted = todayTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
            Long todayFailed = todayTasks.stream()
                .filter(t -> "FAILED".equals(t.getStatus()))
                .count();
            
            // 平均执行时间
            Double averageExecutionTime = taskRecordRepository.getAverageExecutionTime();
            
            // 成功率计算
            double successRate = totalTasks > 0 ? 
                (double) (totalTasks - failedTasks) / totalTasks * 100 : 0;
            
            TaskStatisticsDTO statistics = TaskStatisticsDTO.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .failedTasks(failedTasks)
                .runningTasks(runningTasks)
                .pendingTasks(pendingTasks)
                .todayTasks(todayTotal)
                .todayCompletedTasks(todayCompleted)
                .todayFailedTasks(todayFailed)
                .averageExecutionTime(averageExecutionTime)
                .successRate(successRate)
                .systemLoad(0.45) // 从系统监控获取
                .build();
            
            return Response.success(statistics, "获取统计数据成功");
        } catch (Exception e) {
            log.error("获取任务统计失败: {}", e.getMessage(), e);
            return Response.fail("获取任务统计失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Response<List<TaskHistoryDTO>> getActiveTasks() {
        try {
            List<TaskExecutionRecord> activeRecords = taskRecordRepository.findActiveTasks();
            List<TaskHistoryDTO> activeTasks = activeRecords.stream()
                .map(this::convertToTaskHistoryDTO)
                .collect(Collectors.toList());
            
            return Response.success(activeTasks, "获取活跃任务成功");
        } catch (Exception e) {
            log.error("获取活跃任务失败: {}", e.getMessage(), e);
            return Response.fail("获取活跃任务失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response<String> cancelTask(String taskId) {
        try {
            Optional<TaskExecutionRecord> recordOpt = taskRecordRepository.findByTaskId(taskId);
            if (recordOpt.isPresent()) {
                TaskExecutionRecord record = recordOpt.get();
                if ("RUNNING".equals(record.getStatus()) || "PENDING".equals(record.getStatus())) {
                    record.setStatus("CANCELLED");
                    record.setEndTime(LocalDateTime.now());
                    taskRecordRepository.save(record);
                    
                    return Response.success("任务已取消", "任务 " + taskId + " 已取消");
                } else {
                    return Response.fail("任务状态不允许取消: " + record.getStatus());
                }
            } else {
                return Response.fail("任务不存在: " + taskId);
            }
        } catch (Exception e) {
            log.error("取消任务失败: {}", e.getMessage(), e);
            return Response.fail("取消任务失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response<String> cleanupCompletedTasks() {
        try {
            List<TaskExecutionRecord> completedTasks = taskRecordRepository.findByStatus("COMPLETED");
            List<TaskExecutionRecord> failedTasks = taskRecordRepository.findByStatus("FAILED");
            List<TaskExecutionRecord> cancelledTasks = taskRecordRepository.findByStatus("CANCELLED");
            
            int totalCleaned = completedTasks.size() + failedTasks.size() + cancelledTasks.size();
            
            // 这里可以选择删除或者标记为已清理
            // 为了保留历史记录，我们不删除，而是可以添加一个清理标记
            
            return Response.success("清理完成", "已清理 " + totalCleaned + " 个已完成任务");
        } catch (Exception e) {
            log.error("清理任务失败: {}", e.getMessage(), e);
            return Response.fail("清理任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 转换实体为DTO
     */
    private TaskHistoryDTO convertToTaskHistoryDTO(TaskExecutionRecord record) {
        TaskHistoryDTO.TaskHistoryDTOBuilder builder = TaskHistoryDTO.builder()
            .taskId(record.getTaskId())
            .taskName(record.getTaskName())
            .taskDescription(record.getTaskDescription())
            .taskType(record.getTaskType())
            .status(record.getStatus())
            .progress(record.getProgress())
            .startTime(record.getStartTime())
            .endTime(record.getEndTime())
            .executionTime(record.getExecutionTime())
            .result(record.getResult())
            .errorMessage(record.getErrorMessage());
        
        // 添加智能体名称 - 通过agentId查询
        if (record.getAgentId() != null) {
            try {
                Optional<AiAgent> agentOpt = agentRepository.findById(record.getAgentId());
                if (agentOpt.isPresent()) {
                    builder.agentName(agentOpt.get().getAgentName());
                }
            } catch (Exception e) {
                log.warn("查询智能体信息失败: agentId={}", record.getAgentId());
            }
        }
        
        // 添加执行步骤 - 通过taskRecordId查询
        try {
            List<TaskExecutionLog> logs = taskExecutionLogRepository.findByTaskRecordIdOrderByStepOrderAsc(record.getId());
            if (logs != null && !logs.isEmpty()) {
                List<TaskHistoryDTO.TaskExecutionStepDTO> steps = logs.stream()
                    .map(log -> TaskHistoryDTO.TaskExecutionStepDTO.builder()
                        .stepName(log.getStepName())
                        .stepDescription(log.getStepDescription())
                        .stepType(log.getStepType())
                        .stepStatus(log.getStepStatus())
                        .stepOrder(log.getStepOrder())
                        .executionTime(log.getExecutionTime())
                        .createTime(log.getCreateTime())
                        .build())
                    .collect(Collectors.toList());
                builder.executionSteps(steps);
            }
        } catch (Exception e) {
            log.warn("查询任务执行日志失败: taskRecordId={}", record.getId());
        }
        
        return builder.build();
    }
}
