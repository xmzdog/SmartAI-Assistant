package com.onepiece.xmz.app.agent.controller;

import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.app.agent.service.TaskManagementService;
import com.onepiece.xmz.app.domain.dto.TaskHistoryDTO;
import com.onepiece.xmz.app.domain.dto.TaskStatisticsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理Controller
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin(origins = "*")
public class TaskManagementController {
    
    @Autowired
    private TaskManagementService taskManagementService;
    
    /**
     * 获取任务历史列表
     */
    @GetMapping
    public Response<Page<TaskHistoryDTO>> getTaskHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskType) {
        
        log.info("获取任务历史: page={}, size={}, status={}, taskType={}", page, size, status, taskType);
        return taskManagementService.getTaskHistory(page, size, status, taskType);
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public Response<TaskHistoryDTO> getTaskDetail(@PathVariable String taskId) {
        log.info("获取任务详情: taskId={}", taskId);
        return taskManagementService.getTaskDetail(taskId);
    }
    
    /**
     * 获取任务统计数据
     */
    @GetMapping("/statistics")
    public Response<TaskStatisticsDTO> getTaskStatistics() {
        log.info("获取任务统计数据");
        return taskManagementService.getTaskStatistics();
    }
    
    /**
     * 获取活跃任务列表
     */
    @GetMapping("/active")
    public Response<List<TaskHistoryDTO>> getActiveTasks() {
        log.info("获取活跃任务列表");
        return taskManagementService.getActiveTasks();
    }
    
    /**
     * 取消任务
     */
    @DeleteMapping("/{taskId}")
    public Response<String> cancelTask(@PathVariable String taskId) {
        log.info("取消任务: taskId={}", taskId);
        return taskManagementService.cancelTask(taskId);
    }
    
    /**
     * 清理已完成任务
     */
    @DeleteMapping("/cleanup")
    public Response<String> cleanupCompletedTasks() {
        log.info("清理已完成任务");
        return taskManagementService.cleanupCompletedTasks();
    }
}


