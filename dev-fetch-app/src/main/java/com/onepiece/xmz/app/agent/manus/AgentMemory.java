package com.onepiece.xmz.app.agent.manus;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent记忆管理
 * 参考OpenManus的Memory设计，管理Agent的对话历史和上下文
 * 
 * @author SmartAI-Assistant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentMemory {
    
    /**
     * 存储的消息列表
     */
    private List<AgentMessage> messages = new ArrayList<>();
    
    /**
     * 最大记忆容量
     */
    private int maxCapacity = 100;
    
    /**
     * 是否启用压缩模式
     */
    private boolean compressionEnabled = true;
    
    /**
     * Agent消息包装类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentMessage {
        private String role; // user, assistant, system, tool
        private String content;
        private String toolCallId;
        private String toolName;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;
        
        public AgentMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }
        
        public AgentMessage(String role, String content, String toolCallId, String toolName) {
            this.role = role;
            this.content = content;
            this.toolCallId = toolCallId;
            this.toolName = toolName;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    /**
     * 添加用户消息
     */
    public void addUserMessage(String content) {
        addMessage("user", content);
    }
    
    /**
     * 添加助手消息
     */
    public void addAssistantMessage(String content) {
        addMessage("assistant", content);
    }
    
    /**
     * 添加系统消息
     */
    public void addSystemMessage(String content) {
        addMessage("system", content);
    }
    
    /**
     * 添加工具调用消息
     */
    public void addToolMessage(String content, String toolCallId, String toolName) {
        addMessage(new AgentMessage("tool", content, toolCallId, toolName));
    }
    
    /**
     * 添加消息
     */
    public void addMessage(String role, String content) {
        addMessage(new AgentMessage(role, content));
    }
    
    /**
     * 添加消息到记忆中
     */
    public void addMessage(AgentMessage message) {
        messages.add(message);
        
        // 如果超过容量限制，进行压缩或删除
        if (messages.size() > maxCapacity) {
            if (compressionEnabled) {
                compressMemory();
            } else {
                // 删除最早的消息
                messages.remove(0);
            }
        }
    }
    
    /**
     * 获取所有消息
     */
    public List<AgentMessage> getMessages() {
        return new ArrayList<>(messages);
    }
    
    /**
     * 获取最近的N条消息
     */
    public List<AgentMessage> getRecentMessages(int count) {
        int size = messages.size();
        int startIndex = Math.max(0, size - count);
        return messages.subList(startIndex, size);
    }
    
    /**
     * 获取指定角色的消息
     */
    public List<AgentMessage> getMessagesByRole(String role) {
        return messages.stream()
                .filter(msg -> role.equals(msg.getRole()))
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为SpringAI的Message格式
     */
    public List<Message> toSpringAIMessages() {
        return messages.stream()
                .map(this::convertToSpringAIMessage)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为SpringAI的Message格式（排除工具消息）
     */
    public List<Message> toSpringAIMessagesExcludingTools() {
        return messages.stream()
                .filter(msg -> !"tool".equals(msg.getRole()))
                .map(this::convertToSpringAIMessage)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换单个消息为SpringAI格式
     */
    private Message convertToSpringAIMessage(AgentMessage message) {
        switch (message.getRole()) {
            case "user":
                return new UserMessage(message.getContent());
            case "assistant":
                return new AssistantMessage(message.getContent());
            case "system":
                return new SystemMessage(message.getContent());
            default:
                // 对于tool消息，转换为用户消息格式
                return new UserMessage("Tool result: " + message.getContent());
        }
    }
    
    /**
     * 清空记忆
     */
    public void clear() {
        messages.clear();
    }
    
    /**
     * 获取记忆摘要
     */
    public String getSummary() {
        if (messages.isEmpty()) {
            return "无对话历史";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("总共%d条消息：", messages.size()));
        
        // 统计各类型消息数量
        long userCount = getMessagesByRole("user").size();
        long assistantCount = getMessagesByRole("assistant").size();
        long toolCount = getMessagesByRole("tool").size();
        
        summary.append(String.format("用户消息%d条，助手消息%d条，工具消息%d条", 
                userCount, assistantCount, toolCount));
        
        return summary.toString();
    }
    
    /**
     * 压缩记忆 - 保留重要消息，删除冗余信息
     */
    private void compressMemory() {
        if (messages.size() <= maxCapacity) {
            return;
        }
        
        List<AgentMessage> compressed = new ArrayList<>();
        
        // 保留最近的消息
        int recentCount = maxCapacity / 2;
        List<AgentMessage> recentMessages = getRecentMessages(recentCount);
        compressed.addAll(recentMessages);
        
        // 保留系统消息
        List<AgentMessage> systemMessages = getMessagesByRole("system");
        for (AgentMessage msg : systemMessages) {
            if (!compressed.contains(msg)) {
                compressed.add(0, msg); // 系统消息放在开头
            }
        }
        
        // 如果还是超过容量，删除最旧的消息
        while (compressed.size() > maxCapacity) {
            // 查找并删除最旧的非系统消息
            for (int i = 0; i < compressed.size(); i++) {
                if (!"system".equals(compressed.get(i).getRole())) {
                    compressed.remove(i);
                    break;
                }
            }
        }
        
        this.messages = compressed;
    }
    
    /**
     * 检查是否包含指定内容
     */
    public boolean containsContent(String searchContent) {
        return messages.stream()
                .anyMatch(msg -> msg.getContent().contains(searchContent));
    }
    
    /**
     * 获取上一次工具调用结果
     */
    public String getLastToolResult() {
        return messages.stream()
                .filter(msg -> "tool".equals(msg.getRole()))
                .reduce((first, second) -> second) // 获取最后一个
                .map(AgentMessage::getContent)
                .orElse("");
    }
    
    /**
     * 检查最近是否有重复的助手回复（用于检测循环）
     */
    public boolean hasRecentDuplicateAssistantReplies(int checkCount) {
        List<AgentMessage> assistantMessages = getMessagesByRole("assistant");
        if (assistantMessages.size() < 2) {
            return false;
        }
        
        int checkSize = Math.min(checkCount, assistantMessages.size());
        List<AgentMessage> recentAssistant = assistantMessages
                .subList(assistantMessages.size() - checkSize, assistantMessages.size());
        
        // 检查是否有相同的内容
        for (int i = 0; i < recentAssistant.size() - 1; i++) {
            for (int j = i + 1; j < recentAssistant.size(); j++) {
                if (recentAssistant.get(i).getContent().equals(recentAssistant.get(j).getContent())) {
                    return true;
                }
            }
        }
        
        return false;
    }
}