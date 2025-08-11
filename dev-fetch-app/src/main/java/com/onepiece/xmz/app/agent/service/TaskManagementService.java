package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.app.domain.dto.TaskHistoryDTO;
import com.onepiece.xmz.app.domain.dto.TaskStatisticsDTO;
import com.onepiece.xmz.app.domain.entity.TaskExecutionRecord;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 任务管理服务接口
 * 
 * @author SmartAI-Assistant
 */
public interface TaskManagementService {
    
    /**
     * 创建任务记录
     */
    TaskExecutionRecord createTaskRecord(String taskId, Long agentId, String taskName, 
                                       String taskDescription, String taskType, String inputParams);
    
    /**
     * 更新任务状态
     */
    void updateTaskStatus(String taskId, String status, Integer progress);
    
    /**
     * 完成任务
     */
    void completeTask(String taskId, String result, String outputData);
    
    /**
     * 任务执行失败
     */
    void failTask(String taskId, String errorMessage);
    
    /**
     * 添加任务执行日志
     */
    void addTaskLog(String taskId, String stepName, String stepType, 
                   String stepStatus, String inputData, String outputData);
    
    /**
     * 获取任务详情
     */
    Response<TaskHistoryDTO> getTaskDetail(String taskId);
    
    /**
     * 获取任务历史列表
     */
    Response<Page<TaskHistoryDTO>> getTaskHistory(int page, int size, String status, String taskType);
    
    /**
     * 获取任务统计数据
     */
    Response<TaskStatisticsDTO> getTaskStatistics();
    
    /**
     * 获取活跃任务列表
     */
    Response<List<TaskHistoryDTO>> getActiveTasks();
    
    /**
     * 取消任务
     */
    Response<String> cancelTask(String taskId);
    
    /**
     * 清理已完成的任务
     */
    Response<String> cleanupCompletedTasks();
}


