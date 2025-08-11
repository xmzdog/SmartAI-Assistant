package com.onepiece.xmz.app.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * PDF生成工具类
 * 使用Spring AI的@Tool注解
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class PDFGeneratorTool {
    
    /**
     * 生成PDF文件的响应记录
     */
    public record PDFGenerationResult(
            boolean success,
            String filePath,
            String message,
            long fileSize,
            String generatedAt
    ) {}
    
    /**
     * 生成PDF文件
     * 
     * @param title PDF标题
     * @param content PDF内容
     * @param outputPath 输出路径（可选，默认为temp目录）
     * @return PDF生成结果
     */
    @Tool(description = "Generate a PDF file with given content")
    public PDFGenerationResult generatePDF(
            @ToolParam(description = "Title of the PDF document", required = true) 
            String title,
            @ToolParam(description = "Content to be included in the PDF", required = true) 
            String content,
            @ToolParam(description = "Output file path (optional, defaults to temp directory)")
            String outputPath) {
        
        try {
            log.info("开始生成PDF: 标题={}, 输出路径={}", title, outputPath);
            
            // 如果没有指定输出路径，使用临时目录
            if (outputPath == null || outputPath.trim().isEmpty()) {
                outputPath = System.getProperty("java.io.tmpdir") + File.separator + 
                           "generated_" + System.currentTimeMillis() + ".pdf";
            }
            
            // 确保输出路径以.pdf结尾
            if (!outputPath.toLowerCase().endsWith(".pdf")) {
                outputPath += ".pdf";
            }
            
            // 模拟PDF生成过程（实际应用中应该使用PDF库如iText或Apache PDFBox）
            String pdfContent = generatePDFContent(title, content);
            
            // 创建文件
            File pdfFile = new File(outputPath);
            pdfFile.getParentFile().mkdirs(); // 确保父目录存在
            
            // 写入内容（这里简化为文本文件，实际应该生成真正的PDF）
            try (FileWriter writer = new FileWriter(pdfFile)) {
                writer.write(pdfContent);
            }
            
            long fileSize = pdfFile.length();
            String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            log.info("PDF生成成功: 文件路径={}, 文件大小={}字节", outputPath, fileSize);
            
            return new PDFGenerationResult(
                true, 
                outputPath, 
                "PDF文件生成成功", 
                fileSize, 
                generatedAt
            );
            
        } catch (IOException e) {
            log.error("PDF生成失败: {}", e.getMessage(), e);
            return new PDFGenerationResult(
                false, 
                "", 
                "PDF生成失败: " + e.getMessage(), 
                0, 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }
    
    /**
     * 生成会议纪要PDF
     * 
     * @param meetingTitle 会议标题
     * @param meetingDate 会议日期
     * @param participants 参与者
     * @param agenda 议程
     * @param decisions 决议
     * @param actionItems 行动项
     * @param outputPath 输出路径
     * @return PDF生成结果
     */
    @Tool(description = "Generate a meeting summary PDF with structured content")
    public PDFGenerationResult generateMeetingSummaryPDF(
            @ToolParam(description = "Meeting title", required = true) 
            String meetingTitle,
            @ToolParam(description = "Meeting date (e.g., 2024-01-15)") 
            String meetingDate,
            @ToolParam(description = "List of participants") 
            String participants,
            @ToolParam(description = "Meeting agenda") 
            String agenda,
            @ToolParam(description = "Meeting decisions") 
            String decisions,
            @ToolParam(description = "Action items") 
            String actionItems,
            @ToolParam(description = "Output file path (optional)") 
            String outputPath) {
        
        // 构建会议纪要内容
        StringBuilder content = new StringBuilder();
        content.append("会议日期: ").append(meetingDate != null ? meetingDate : "未指定").append("\n\n");
        
        if (participants != null && !participants.trim().isEmpty()) {
            content.append("参与者:\n").append(participants).append("\n\n");
        }
        
        if (agenda != null && !agenda.trim().isEmpty()) {
            content.append("议程:\n").append(agenda).append("\n\n");
        }
        
        if (decisions != null && !decisions.trim().isEmpty()) {
            content.append("会议决议:\n").append(decisions).append("\n\n");
        }
        
        if (actionItems != null && !actionItems.trim().isEmpty()) {
            content.append("行动项:\n").append(actionItems).append("\n\n");
        }
        
        // 调用通用PDF生成方法
        return generatePDF(meetingTitle, content.toString(), outputPath);
    }
    
    /**
     * 生成任务执行计划PDF
     * 
     * @param planTitle 计划标题
     * @param objective 目标
     * @param steps 执行步骤
     * @param expectedResults 预期结果
     * @param outputPath 输出路径
     * @return PDF生成结果
     */
    @Tool(description = "Generate an execution plan PDF with objectives, steps, and expected results")
    public PDFGenerationResult generateExecutionPlanPDF(
            @ToolParam(description = "Plan title", required = true) 
            String planTitle,
            @ToolParam(description = "Objective of the plan") 
            String objective,
            @ToolParam(description = "Execution steps") 
            String steps,
            @ToolParam(description = "Expected results") 
            String expectedResults,
            @ToolParam(description = "Output file path (optional)") 
            String outputPath) {
        
        // 构建执行计划内容
        StringBuilder content = new StringBuilder();
        
        if (objective != null && !objective.trim().isEmpty()) {
            content.append("目标:\n").append(objective).append("\n\n");
        }
        
        if (steps != null && !steps.trim().isEmpty()) {
            content.append("执行步骤:\n").append(steps).append("\n\n");
        }
        
        if (expectedResults != null && !expectedResults.trim().isEmpty()) {
            content.append("预期结果:\n").append(expectedResults).append("\n\n");
        }
        
        // 调用通用PDF生成方法
        return generatePDF(planTitle, content.toString(), outputPath);
    }
    
    /**
     * 生成PDF内容
     */
    private String generatePDFContent(String title, String content) {
        StringBuilder pdfContent = new StringBuilder();
        
        // PDF头部信息
        pdfContent.append("==========================================\n");
        pdfContent.append("           PDF 文档\n");
        pdfContent.append("==========================================\n\n");
        
        // 标题
        pdfContent.append("标题: ").append(title).append("\n");
        pdfContent.append("生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        pdfContent.append("------------------------------------------\n");
        pdfContent.append("内容:\n");
        pdfContent.append("------------------------------------------\n\n");
        
        // 内容
        pdfContent.append(content);
        
        pdfContent.append("\n\n==========================================\n");
        pdfContent.append("           文档结束\n");
        pdfContent.append("==========================================\n");
        
        return pdfContent.toString();
    }
}