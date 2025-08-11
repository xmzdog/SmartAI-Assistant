package com.onepiece.xmz.app.agent.manus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ManusAgent演示类
 * 演示基于OpenManus架构的分层智能体系统
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class ManusAgentDemo implements CommandLineRunner {
    
    @Autowired
    private ManusAgentService manusAgentService;
    
    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "manus-demo".equals(args[0])) {
            log.info("=== ManusAgent演示开始 ===");
            
            // 演示1：基础任务执行
            demonstrateBasicExecution();
            
            // 演示2：深度推理能力
            demonstrateDeepReasoning();
            
            // 演示3：复杂任务处理
            demonstrateComplexTask();
            
            // 演示4：Agent配置管理
            demonstrateAgentConfiguration();
            
            log.info("=== ManusAgent演示完成 ===");
        }
    }
    
    /**
     * 演示基础任务执行
     */
    private void demonstrateBasicExecution() {
        log.info("\n--- 演示1：基础任务执行 ---");
        
        try {
            String task = "请简单介绍一下SpringAI框架的主要功能";
            log.info("执行任务: {}", task);
            
            String result = manusAgentService.executeTask(task);
            log.info("执行结果: \n{}", result);
            
        } catch (Exception e) {
            log.error("基础任务执行演示失败", e);
        }
    }
    
    /**
     * 演示深度推理能力
     */
    private void demonstrateDeepReasoning() {
        log.info("\n--- 演示2：深度推理能力 ---");
        
        try {
            String problem = "为什么微服务架构比单体架构更适合大型系统？请从多个维度进行分析。";
            log.info("推理问题: {}", problem);
            
            String result = manusAgentService.performDeepReasoning(problem, "COMPREHENSIVE");
            log.info("推理结果: \n{}", result);
            
        } catch (Exception e) {
            log.error("深度推理演示失败", e);
        }
    }
    
    /**
     * 演示复杂任务处理
     */
    private void demonstrateComplexTask() {
        log.info("\n--- 演示3：复杂任务处理 ---");
        
        try {
            String complexTask = "请执行以下复杂任务：\n" +
                    "                    1. 分析SpringAI在企业AI应用中的优势\n" +
                    "                    2. 搜索相关的最新技术趋势\n" +
                    "                    3. 生成一份技术评估报告\n" +
                    "                    4. 提出具体的实施建议";
            
            log.info("执行复杂任务: {}", complexTask);
            
            String result = manusAgentService.executeTask(complexTask);
            log.info("复杂任务结果: \n{}", result);
            
        } catch (Exception e) {
            log.error("复杂任务演示失败", e);
        }
    }
    
    /**
     * 演示Agent配置管理
     */
    private void demonstrateAgentConfiguration() {
        log.info("\n--- 演示4：Agent配置管理 ---");
        
        try {
            // 获取当前状态
            log.info("Agent当前状态:");
            String status = manusAgentService.getAgentStatus();
            log.info(status);
            
            // 修改配置
            log.info("\n修改Agent配置:");
            String configResult1 = manusAgentService.configureAgent("cot_enabled", "true");
            log.info(configResult1);
            
            String configResult2 = manusAgentService.configureAgent("tool_selection_strategy", "HYBRID");
            log.info(configResult2);
            
            String configResult3 = manusAgentService.configureAgent("max_steps", "20");
            log.info(configResult3);
            
            // 获取运行统计
            log.info("\n运行统计:");
            String stats = manusAgentService.getRunningStatistics();
            log.info(stats);
            
        } catch (Exception e) {
            log.error("配置管理演示失败", e);
        }
    }
    
    /**
     * 演示异步任务执行
     */
    private void demonstrateAsyncExecution() {
        log.info("\n--- 演示：异步任务执行 ---");
        
        try {
            String asyncTask = "请分析人工智能发展的三个主要阶段，并预测未来趋势";
            String taskId = "demo-async-001";
            
            log.info("启动异步任务: {} (ID: {})", asyncTask, taskId);
            
            String startResult = manusAgentService.executeTaskAsync(asyncTask, taskId);
            log.info("启动结果: {}", startResult);
            
            // 等待一段时间
            Thread.sleep(2000);
            
            // 检查状态
            String status = manusAgentService.getTaskStatus(taskId);
            log.info("任务状态: {}", status);
            
            // 等待任务完成
            Thread.sleep(5000);
            
            // 获取结果
            String result = manusAgentService.getTaskResult(taskId);
            log.info("任务结果: \n{}", result);
            
        } catch (Exception e) {
            log.error("异步任务演示失败", e);
        }
    }
    
    /**
     * 演示不同推理策略
     */
    private void demonstrateReasoningStrategies() {
        log.info("\n--- 演示：不同推理策略 ---");
        
        String problem = "如何解决项目中的技术债务问题？";
        
        String[] strategies = {"STEP_BY_STEP", "PROBLEM_DECOMPOSITION", "ANALYTICAL", "COMPREHENSIVE"};
        
        for (String strategy : strategies) {
            try {
                log.info("\n使用{}策略推理:", strategy);
                
                String result = manusAgentService.performDeepReasoning(problem, strategy);
                log.info("推理结果摘要: {}", 
                        result.length() > 200 ? result.substring(0, 200) + "..." : result);
                
            } catch (Exception e) {
                log.error("推理策略{}演示失败", strategy, e);
            }
        }
    }
    
    /**
     * 演示错误处理和恢复
     */
    private void demonstrateErrorHandling() {
        log.info("\n--- 演示：错误处理和恢复 ---");
        
        try {
            // 模拟一个可能导致错误的任务
            String errorTask = "请调用一个不存在的工具进行测试";
            log.info("执行错误任务: {}", errorTask);
            
            String result = manusAgentService.executeTask(errorTask);
            log.info("错误处理结果: {}", result);
            
            // 重启Agent进行恢复
            log.info("重启Agent进行恢复...");
            String restartResult = manusAgentService.restartAgent();
            log.info("重启结果: {}", restartResult);
            
        } catch (Exception e) {
            log.error("错误处理演示失败", e);
        }
    }
}