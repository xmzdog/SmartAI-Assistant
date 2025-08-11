package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.app.domain.entity.*;
import com.onepiece.xmz.app.domain.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 数据初始化服务
 * 应用启动时初始化基础数据
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Service
public class DataInitializationService implements ApplicationRunner {
    
    @Autowired
    private AiAgentRepository agentRepository;
    
    @Autowired
    private AiRagOrderRepository ragOrderRepository;
    
    @Autowired
    private SystemMonitorDataRepository monitorDataRepository;
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化系统基础数据");
        
        try {
            // 初始化默认智能体（如果不存在）
            initializeDefaultAgents();
            
            // 初始化默认知识库配置（如果不存在）
            initializeDefaultRagOrders();
            
            // 初始化系统监控数据
            initializeSystemMonitorData();
            
            log.info("系统基础数据初始化完成");
            
        } catch (Exception e) {
            log.error("系统数据初始化失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 初始化默认智能体
     */
    private void initializeDefaultAgents() {
        try {
            // 检查是否已有智能体数据
            long agentCount = agentRepository.count();
            if (agentCount == 0) {
                log.info("初始化默认智能体配置");
                
                // 创建ManusAgent
                AiAgent manusAgent = new AiAgent();
                manusAgent.setAgentName("ManusAgent");
                manusAgent.setDescription("通用多功能智能体，支持复杂任务执行和深度推理");
                manusAgent.setChannel("agent");
                manusAgent.setStatus(1);
                agentRepository.save(manusAgent);
                
                // 创建RAG智能体
                AiAgent ragAgent = new AiAgent();
                ragAgent.setAgentName("RAGAgent");
                ragAgent.setDescription("知识库问答智能体，专门处理文档检索和智能问答");
                ragAgent.setChannel("rag");
                ragAgent.setStatus(1);
                agentRepository.save(ragAgent);
                
                log.info("默认智能体初始化完成");
            }
        } catch (Exception e) {
            log.error("初始化默认智能体失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 初始化默认知识库配置
     */
    private void initializeDefaultRagOrders() {
        try {
            // 检查是否已有知识库配置
            long ragCount = ragOrderRepository.count();
            if (ragCount == 0) {
                log.info("初始化默认知识库配置");
                
                // 创建默认知识库
                AiRagOrder defaultRag = new AiRagOrder();
                defaultRag.setRagName("默认知识库");
                defaultRag.setKnowledgeTag("default");
                defaultRag.setStatus(1);
                ragOrderRepository.save(defaultRag);
                
                // 创建技术文档知识库
                AiRagOrder techRag = new AiRagOrder();
                techRag.setRagName("技术文档知识库");
                techRag.setKnowledgeTag("技术文档");
                techRag.setStatus(1);
                ragOrderRepository.save(techRag);
                
                log.info("默认知识库配置初始化完成");
            }
        } catch (Exception e) {
            log.error("初始化默认知识库配置失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 初始化系统监控数据
     */
    private void initializeSystemMonitorData() {
        try {
            // 创建初始监控数据点
            SystemMonitorData initialData = new SystemMonitorData();
            initialData.setMonitorTime(LocalDateTime.now());
            initialData.setCpuUsage(15.5);
            initialData.setMemoryUsage(45.2);
            initialData.setDiskUsage(35.8);
            initialData.setActiveTasks(0);
            initialData.setCompletedTasksToday(0);
            initialData.setFailedTasksToday(0);
            initialData.setAverageResponseTime(1000.0);
            initialData.setSystemLoad(0.2);
            initialData.setAgentStatus("IDLE");
            initialData.setUptimeSeconds(0L);
            initialData.setLastActivity(LocalDateTime.now());
            
            monitorDataRepository.save(initialData);
            log.info("初始系统监控数据创建完成");
            
        } catch (Exception e) {
            log.error("初始化系统监控数据失败: {}", e.getMessage(), e);
        }
    }
}


