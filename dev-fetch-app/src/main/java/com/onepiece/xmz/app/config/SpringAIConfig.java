package com.onepiece.xmz.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpringAI配置类
 * 配置ChatClient和相关组件
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Configuration
public class SpringAIConfig {
    
    /**
     * 配置聊天记忆存储 - 使用内存Map存储对话历史
     */
    @Bean
    @Primary 
    public Map<Object, Object> chatMemoryStore() {
        log.info("初始化ChatMemory存储");
        return new ConcurrentHashMap<>();
    }
    
    /**
     * 配置默认的ChatClient.Builder
     * 这将被SpringAI自动配置使用
     */
    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel, Map<Object, Object> chatMemoryStore) {
        log.info("初始化ChatClient.Builder");
        
        return ChatClient.builder(chatModel)
//                .defaultAdvisors(new SimpleChatMemoryAdvisor(chatMemoryStore))
                .defaultSystem("""
                    你是一个专业的AI助手，擅长任务规划和执行。你的主要职责包括：
                    
                    1. 理解和分析用户的复杂任务需求
                    2. 将复杂任务分解为可执行的具体步骤
                    3. 智能选择和调用合适的工具来完成任务
                    4. 处理执行过程中的错误和异常情况
                    5. 生成详细的执行报告和总结
                    
                    可用的工具包括：
                    - RAG知识库搜索和问答
                    - 腾讯会议API调用（获取会议、下载录制、转录音频）
                    - PDF报告生成（执行计划、会议纪要等）
                    - 网络搜索获取最新信息
                    
                    请始终：
                    - 提供清晰的步骤说明
                    - 使用最合适的工具
                    - 给出详细的执行反馈
                    - 处理错误时提供解决方案
                    """);
    }
    
    /**
     * 配置专用的Agent ChatClient
     * 用于Agent服务中的复杂任务处理
     */
    @Bean("agentChatClient")
    public ChatClient agentChatClient(
            ChatClient.Builder builder,
            com.onepiece.xmz.app.agent.tools.RAGTool ragTool,
            com.onepiece.xmz.app.agent.tools.TencentMeetingTool tencentMeetingTool,
            com.onepiece.xmz.app.agent.tools.PDFTool pdfTool,
            com.onepiece.xmz.app.agent.tools.WebSearchTool webSearchTool) {
        log.info("初始化专用的Agent ChatClient with tools");
        
        return builder
                .defaultSystem("""
                    你是一个智能任务执行代理(Agent)。你需要：
                    
                    1. 分析任务需求和上下文
                    2. 制定详细的执行计划
                    3. 按步骤调用工具执行任务
                    4. 监控执行进度和处理异常
                    5. 生成完整的执行报告
                    
                    执行原则：
                    - 任务分解要具体可行
                    - 工具选择要精准有效
                    - 错误处理要完善
                    - 反馈信息要详细
                    
                    当前支持的工具函数：
                    - ragSearch: RAG语义搜索
                    - ragQA: RAG知识问答
                    - getMeetings: 获取会议列表
                    - getMeetingDetail: 获取会议详情
                    - downloadMeetingRecord: 下载会议录制
                    - transcribeMeeting: 转录会议音频
                    - generatePdfReport: 生成PDF报告
                    - generateExecutionPlanPdf: 生成执行计划PDF
                    - generateMeetingSummaryPdf: 生成会议纪要PDF
                    - webSearch: 网络搜索
                    
                    请根据任务需求智能选择和组合使用这些工具。
                    """)
                .defaultTools(
                        ragTool,              // RAG工具
                        tencentMeetingTool,   // 腾讯会议工具
                        pdfTool,              // PDF生成工具
                        webSearchTool         // 网络搜索工具
                )
                .build();
    }
}