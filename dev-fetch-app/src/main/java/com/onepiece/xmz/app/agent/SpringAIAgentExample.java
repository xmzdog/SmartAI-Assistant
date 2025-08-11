package com.onepiece.xmz.app.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SpringAI Agent系统示例
 * 演示如何使用基于SpringAI的智能Agent
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class SpringAIAgentExample implements CommandLineRunner {
    
    @Autowired
    @Qualifier("agentChatClient") 
    private ChatClient agentChatClient;
    
    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "demo".equals(args[0])) {
            log.info("=== SpringAI Agent系统演示 ===");
            
            // 演示1：简单的RAG问答
            demonstrateRAGQuery();
            
            // 演示2：会议分析任务
            demonstrateMeetingAnalysis();
            
            // 演示3：复合任务执行
            demonstrateComplexTask();
            
            log.info("=== SpringAI Agent演示完成 ===");
        }
    }
    
    /**
     * 演示RAG知识问答功能
     */
    private void demonstrateRAGQuery() {
        log.info("\n--- 演示1：RAG知识问答 ---");
        
        try {
            String response = agentChatClient.prompt()
                    .user("请在knowledge base中搜索关于'SpringAI'的信息，并总结主要内容")
                    .call()
                    .content();
            
            log.info("RAG问答结果：\n{}", response);
            
        } catch (Exception e) {
            log.error("RAG问答演示失败", e);
        }
    }
    
    /**
     * 演示会议分析任务
     */
    private void demonstrateMeetingAnalysis() {
        log.info("\n--- 演示2：会议分析任务 ---");
        
        try {
            String taskDescription = """
                请执行以下会议分析任务：
                1. 获取2024年1月15日的会议列表
                2. 选择重要的会议获取详细信息
                3. 如果有录制文件，下载并转录音频
                4. 生成会议纪要PDF报告
                """;
            
            String response = agentChatClient.prompt()
                    .user(taskDescription)
                    .call()
                    .content();
            
            log.info("会议分析结果：\n{}", response);
            
        } catch (Exception e) {
            log.error("会议分析演示失败", e);
        }
    }
    
    /**
     * 演示复合任务执行
     */
    private void demonstrateComplexTask() {
        log.info("\n--- 演示3：复合任务执行 ---");
        
        try {
            String taskDescription = """
                请完成以下复合任务：
                1. 搜索"SpringAI ChatClient"相关信息
                2. 结合我们知识库中的SpringAI文档
                3. 网络搜索最新的SpringAI发展趋势
                4. 生成一份综合性的技术报告PDF
                
                报告应包含：
                - SpringAI ChatClient的核心功能
                - 最佳实践和应用场景
                - 未来发展趋势
                """;
            
            String response = agentChatClient.prompt()
                    .user(taskDescription)
                    .advisors(advisor -> advisor
                            .param("task_type", "complex_research")
                            .param("output_format", "detailed_report")
                    )
                    .call()
                    .content();
            
            log.info("复合任务执行结果：\n{}", response);
            
        } catch (Exception e) {
            log.error("复合任务演示失败", e);
        }
    }
    
    /**
     * 演示流式响应处理
     */
    public void demonstrateStreamingResponse() {
        log.info("\n--- 演示：流式响应处理 ---");
        
        try {
            agentChatClient.prompt()
                    .user("请分步骤说明如何使用SpringAI构建智能Agent系统")
                    .stream()
                    .content()
                    .subscribe(
                            content -> log.info("流式内容: {}", content),
                            error -> log.error("流式处理错误", error),
                            () -> log.info("流式处理完成")
                    );
            
        } catch (Exception e) {
            log.error("流式响应演示失败", e);
        }
    }
    
    /**
     * 演示Function Calling
     */
    public void demonstrateFunctionCalling() {
        log.info("\n--- 演示：Function Calling ---");
        
        try {
            String response = agentChatClient.prompt()
                    .user("请使用ragSearch函数搜索'SpringAI'相关内容，然后用webSearch查找最新资讯")
                    .call()
                    .content();
            
            log.info("Function Calling结果：\n{}", response);
            
        } catch (Exception e) {
            log.error("Function Calling演示失败", e);
        }
    }
}