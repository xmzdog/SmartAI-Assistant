package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.app.domain.entity.SystemMonitorData;
import com.onepiece.xmz.app.domain.repository.TaskExecutionRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;

/**
 * 系统监控数据定时收集器
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class SystemMonitorScheduler {
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    @Autowired
    private TaskExecutionRecordRepository taskRecordRepository;
    
    private final LocalDateTime systemStartTime = LocalDateTime.now();
    
    /**
     * 每5分钟收集一次系统监控数据
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void collectSystemMonitorData() {
        try {
            log.debug("开始收集系统监控数据");
            
            SystemMonitorData monitorData = new SystemMonitorData();
            monitorData.setMonitorTime(LocalDateTime.now());
            
            // 收集系统性能数据
            monitorData.setCpuUsage(getCurrentCpuUsage());
            monitorData.setMemoryUsage(getCurrentMemoryUsage());
            monitorData.setDiskUsage(50.0); // 模拟磁盘使用率
            
            // 收集任务相关数据
            int activeTasks = taskRecordRepository.findActiveTasks().size();
            monitorData.setActiveTasks(activeTasks);
            
            Long todayCompleted = taskRecordRepository.countTodayTasksByStatus("COMPLETED");
            Long todayFailed = taskRecordRepository.countTodayTasksByStatus("FAILED");
            
            monitorData.setCompletedTasksToday(todayCompleted != null ? todayCompleted.intValue() : 0);
            monitorData.setFailedTasksToday(todayFailed != null ? todayFailed.intValue() : 0);
            
            // 计算平均响应时间
            Double avgExecutionTime = taskRecordRepository.getAverageExecutionTime();
            monitorData.setAverageResponseTime(avgExecutionTime != null ? avgExecutionTime : 1200.0);
            
            // 计算系统负载
            monitorData.setSystemLoad(getCurrentSystemLoad());
            
            // 确定Agent状态
            String agentStatus = activeTasks > 0 ? "BUSY" : "IDLE";
            monitorData.setAgentStatus(agentStatus);
            
            // 计算运行时间
            long uptimeSeconds = java.time.Duration.between(systemStartTime, LocalDateTime.now()).getSeconds();
            monitorData.setUptimeSeconds(uptimeSeconds);
            
            monitorData.setLastActivity(LocalDateTime.now());
            
            // 保存监控数据
            systemMonitorService.recordMonitorData(monitorData);
            
            log.debug("系统监控数据收集完成");
            
        } catch (Exception e) {
            log.error("收集系统监控数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每天凌晨清理过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void cleanupOldData() {
        try {
            log.info("开始清理过期监控数据");
            systemMonitorService.cleanupOldMonitorData();
            log.info("过期监控数据清理完成");
        } catch (Exception e) {
            log.error("清理过期监控数据失败: {}", e.getMessage(), e);
        }
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
                double cpuLoad = sunOsBean.getProcessCpuLoad();
                return cpuLoad > 0 ? cpuLoad * 100 : 25.0;
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
            return max > 0 ? (double) used / max * 100 : 65.0;
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
            double systemLoadAverage = osBean.getSystemLoadAverage();
            int availableProcessors = osBean.getAvailableProcessors();
            
            if (systemLoadAverage > 0 && availableProcessors > 0) {
                return systemLoadAverage / availableProcessors;
            }
            return 0.4; // 默认值
        } catch (Exception e) {
            return 0.4; // 默认值
        }
    }
}


