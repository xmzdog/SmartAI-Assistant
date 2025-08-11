package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.app.domain.dto.AgentStatusDTO;
import com.onepiece.xmz.app.domain.entity.SystemMonitorData;

import java.util.List;

/**
 * 系统监控服务接口
 * 
 * @author SmartAI-Assistant
 */
public interface SystemMonitorService {
    
    /**
     * 记录系统监控数据
     */
    void recordMonitorData(SystemMonitorData monitorData);
    
    /**
     * 获取当前Agent状态
     */
    Response<AgentStatusDTO> getCurrentAgentStatus();
    
    /**
     * 获取最近的监控数据
     */
    Response<List<SystemMonitorData>> getRecentMonitorData(int limit);
    
    /**
     * 获取系统健康状态
     */
    Response<String> getSystemHealth();
    
    /**
     * 更新Agent状态
     */
    void updateAgentStatus(String status, Integer activeTaskCount);
    
    /**
     * 记录Agent活动
     */
    void recordAgentActivity();
    
    /**
     * 清理过期监控数据
     */
    void cleanupOldMonitorData();
    
    /**
     * 获取系统运行时间
     */
    String getSystemUptime();
}


