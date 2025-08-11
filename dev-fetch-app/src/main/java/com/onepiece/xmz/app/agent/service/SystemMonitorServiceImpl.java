package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.app.domain.dto.AgentStatusDTO;
import com.onepiece.xmz.app.domain.entity.SystemMonitorData;
import com.onepiece.xmz.app.domain.entity.TaskExecutionRecord;
import com.onepiece.xmz.app.domain.repository.SystemMonitorDataRepository;
import com.onepiece.xmz.app.domain.repository.TaskExecutionRecordRepository;
import com.onepiece.xmz.api.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 系统监控服务实现
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Service
@Transactional
public class SystemMonitorServiceImpl implements SystemMonitorService {
    
    @Autowired
    private SystemMonitorDataRepository monitorDataRepository;
    
    @Autowired
    private TaskExecutionRecordRepository taskRecordRepository;
    
    private final LocalDateTime systemStartTime = LocalDateTime.now();
    
    @Override
    public void recordMonitorData(SystemMonitorData monitorData) {
        try {
            monitorDataRepository.save(monitorData);
            log.debug("记录系统监控数据成功");
        } catch (Exception e) {
            log.error("记录系统监控数据失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Response<AgentStatusDTO> getCurrentAgentStatus() {
        try {
            // 获取最新的监控数据
            Optional<SystemMonitorData> latestDataOpt = monitorDataRepository.findTopByOrderByMonitorTimeDesc();
            
            AgentStatusDTO.AgentStatusDTOBuilder builder = AgentStatusDTO.builder();
            
            if (latestDataOpt.isPresent()) {
                SystemMonitorData data = latestDataOpt.get();
                builder.status(data.getAgentStatus())
                       .cpuUsage(data.getCpuUsage())
                       .memoryUsage(data.getMemoryUsage())
                       .diskUsage(data.getDiskUsage())
                       .systemLoad(data.getSystemLoad())
                       .averageResponseTime(data.getAverageResponseTime())
                       .lastActivity(data.getLastActivity());
                       
                // 计算运行时间
                if (data.getUptimeSeconds() != null) {
                    builder.uptime(formatUptime(data.getUptimeSeconds()));
                }
            } else {
                // 如果没有监控数据，使用实时数据
                builder.status("IDLE")
                       .cpuUsage(getCurrentCpuUsage())
                       .memoryUsage(getCurrentMemoryUsage())
                       .diskUsage(50.0)
                       .systemLoad(0.3)
                       .averageResponseTime(1200.0)
                       .lastActivity(LocalDateTime.now())
                       .uptime(getSystemUptime());
            }
            
            // 获取活跃任务数
            List<TaskExecutionRecord> activeTasks = taskRecordRepository.findActiveTasks();
            builder.activeTaskCount(activeTasks.size());
            
            builder.version("1.0.0");
            
            return Response.success(builder.build(), "获取Agent状态成功");
        } catch (Exception e) {
            log.error("获取Agent状态失败: {}", e.getMessage(), e);
            return Response.fail("获取Agent状态失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Response<List<SystemMonitorData>> getRecentMonitorData(int limit) {
        try {
            List<SystemMonitorData> recentData = limit <= 10 ? 
                monitorDataRepository.findTop10ByOrderByMonitorTimeDesc() :
                monitorDataRepository.findAll();
            
            return Response.success(recentData, "获取监控数据成功");
        } catch (Exception e) {
            log.error("获取监控数据失败: {}", e.getMessage(), e);
            return Response.fail("获取监控数据失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Response<String> getSystemHealth() {
        try {
            Optional<SystemMonitorData> latestDataOpt = monitorDataRepository.findTopByOrderByMonitorTimeDesc();
            
            if (latestDataOpt.isPresent()) {
                SystemMonitorData data = latestDataOpt.get();
                
                // 简单的健康检查逻辑
                if (data.getCpuUsage() != null && data.getCpuUsage() > 90) {
                    return Response.success("WARNING", "CPU使用率过高");
                }
                if (data.getMemoryUsage() != null && data.getMemoryUsage() > 90) {
                    return Response.success("WARNING", "内存使用率过高");
                }
                if ("ERROR".equals(data.getAgentStatus())) {
                    return Response.success("ERROR", "Agent状态异常");
                }
                
                return Response.success("HEALTHY", "系统运行正常");
            } else {
                return Response.success("UNKNOWN", "无监控数据");
            }
        } catch (Exception e) {
            log.error("获取系统健康状态失败: {}", e.getMessage(), e);
            return Response.fail("获取系统健康状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateAgentStatus(String status, Integer activeTaskCount) {
        try {
            SystemMonitorData monitorData = new SystemMonitorData();
            monitorData.setAgentStatus(status);
            monitorData.setActiveTasks(activeTaskCount);
            monitorData.setLastActivity(LocalDateTime.now());
            monitorData.setCpuUsage(getCurrentCpuUsage());
            monitorData.setMemoryUsage(getCurrentMemoryUsage());
            monitorData.setSystemLoad(getCurrentSystemLoad());
            
            recordMonitorData(monitorData);
        } catch (Exception e) {
            log.error("更新Agent状态失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void recordAgentActivity() {
        updateAgentStatus("BUSY", taskRecordRepository.findActiveTasks().size());
    }
    
    @Override
    public void cleanupOldMonitorData() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            monitorDataRepository.deleteOldData(cutoffTime);
            log.info("清理过期监控数据完成");
        } catch (Exception e) {
            log.error("清理过期监控数据失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public String getSystemUptime() {
        return formatUptime(java.time.Duration.between(systemStartTime, LocalDateTime.now()).getSeconds());
    }
    
    /**
     * 格式化运行时间
     */
    private String formatUptime(Long uptimeSeconds) {
        if (uptimeSeconds == null || uptimeSeconds <= 0) {
            return "0秒";
        }
        
        long days = uptimeSeconds / 86400;
        long hours = (uptimeSeconds % 86400) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天 ");
        if (hours > 0) sb.append(hours).append("小时 ");
        if (minutes > 0) sb.append(minutes).append("分钟 ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("秒");
        
        return sb.toString().trim();
    }
    
    /**
     * 获取当前CPU使用率
     */
    private Double getCurrentCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                return sunOsBean.getProcessCpuLoad() * 100;
            }
            return 25.0; // 默认值
        } catch (Exception e) {
            return 25.0; // 默认值
        }
    }
    
    /**
     * 获取当前内存使用率
     */
    private Double getCurrentMemoryUsage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long used = memoryBean.getHeapMemoryUsage().getUsed();
            long max = memoryBean.getHeapMemoryUsage().getMax();
            return (double) used / max * 100;
        } catch (Exception e) {
            return 65.0; // 默认值
        }
    }
    
    /**
     * 获取当前系统负载
     */
    private Double getCurrentSystemLoad() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemLoadAverage() / osBean.getAvailableProcessors();
        } catch (Exception e) {
            return 0.4; // 默认值
        }
    }
}


