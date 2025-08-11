package com.onepiece.xmz.app.agent.controller;

import com.onepiece.xmz.app.agent.service.SystemMonitorService;
import com.onepiece.xmz.app.domain.dto.AgentStatusDTO;
import com.onepiece.xmz.app.domain.entity.SystemMonitorData;
import com.onepiece.xmz.api.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统监控Controller
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/monitor")
@CrossOrigin(origins = "*")
public class SystemMonitorController {
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    /**
     * 获取当前Agent状态
     */
    @GetMapping("/status")
    public Response<AgentStatusDTO> getAgentStatus() {
        log.info("获取Agent状态");
        return systemMonitorService.getCurrentAgentStatus();
    }
    
    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public Response<String> getSystemHealth() {
        log.info("获取系统健康状态");
        return systemMonitorService.getSystemHealth();
    }
    
    /**
     * 获取最近监控数据
     */
    @GetMapping("/data")
    public Response<List<SystemMonitorData>> getMonitorData(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("获取监控数据: limit={}", limit);
        return systemMonitorService.getRecentMonitorData(limit);
    }
    
    /**
     * 手动记录Agent活动
     */
    @PostMapping("/activity")
    public Response<String> recordActivity() {
        log.info("记录Agent活动");
        try {
            systemMonitorService.recordAgentActivity();
            return Response.success("活动记录成功", "Agent活动已记录");
        } catch (Exception e) {
            log.error("记录Agent活动失败: {}", e.getMessage(), e);
            return Response.fail("记录活动失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期监控数据
     */
    @DeleteMapping("/cleanup")
    public Response<String> cleanupOldData() {
        log.info("清理过期监控数据");
        try {
            systemMonitorService.cleanupOldMonitorData();
            return Response.success("清理完成", "过期监控数据已清理");
        } catch (Exception e) {
            log.error("清理监控数据失败: {}", e.getMessage(), e);
            return Response.fail("清理失败: " + e.getMessage());
        }
    }
}


