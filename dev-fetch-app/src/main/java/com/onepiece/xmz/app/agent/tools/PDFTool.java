package com.onepiece.xmz.app.agent.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PDF报告生成工具
 * 基于SpringAI Tools机制
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class PDFTool {
    
    /**
     * 生成PDF报告请求
     */
    @JsonClassDescription("生成PDF报告请求")
    public record GeneratePDFRequest(
            @JsonPropertyDescription("报告标题") String title,
            @JsonPropertyDescription("报告内容") String content,
            @JsonPropertyDescription("模板类型：report/plan/summary") String templateType,
            @JsonPropertyDescription("输出文件路径，可选") String outputPath,
            @JsonPropertyDescription("额外参数") Map<String, Object> parameters
    ) {}
    
    /**
     * 生成PDF报告响应
     */
    public record GeneratePDFResponse(
            String title,
            String filePath,
            long fileSize,
            int pageCount,
            String generatedTime,
            boolean success,
            String message
    ) {}
    
    /**
     * 生成执行计划PDF请求
     */
    @JsonClassDescription("生成执行计划PDF请求")
    public record GenerateExecutionPlanPDFRequest(
            @JsonPropertyDescription("计划标题") String title,
            @JsonPropertyDescription("任务描述") String taskDescription,
            @JsonPropertyDescription("执行步骤列表") List<String> steps,
            @JsonPropertyDescription("预期结果") String expectedResult,
            @JsonPropertyDescription("输出路径，可选") String outputPath
    ) {}
    
    /**
     * 生成会议纪要PDF请求
     */
    @JsonClassDescription("生成会议纪要PDF请求")
    public record GenerateMeetingSummaryPDFRequest(
            @JsonPropertyDescription("会议标题") String meetingTitle,
            @JsonPropertyDescription("会议时间") String meetingTime,
            @JsonPropertyDescription("参与者列表") List<String> participants,
            @JsonPropertyDescription("会议内容") String content,
            @JsonPropertyDescription("关键决议") List<String> decisions,
            @JsonPropertyDescription("行动项") List<String> actionItems,
            @JsonPropertyDescription("输出路径，可选") String outputPath
    ) {}
    
    /**
     * 通用PDF报告生成工具
     */
    @Tool(description = "生成PDF格式的报告文档，支持多种模板和自定义内容")
    public GeneratePDFResponse generatePdfReport(GeneratePDFRequest request) {
        try {
            // 检查参数是否为空
            if (request == null) {
                log.error("PDF报告生成请求参数为空");
                return new GeneratePDFResponse(
                        "未知标题",
                        "",
                        0,
                        0,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        false,
                        "请求参数不能为空"
                );
            }
            
            log.info("生成PDF报告: {} - 模板: {} - 输出: {}", 
                    request.title(), request.templateType(), request.outputPath());
            
            // 模拟PDF生成过程
            GeneratePDFResponse response = simulateGeneratePDF(request);
            
            if (response.success()) {
                log.info("PDF报告生成成功: {}", response.filePath());
            } else {
                log.warn("PDF报告生成失败: {}", response.message());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("PDF报告生成异常: {}", e.getMessage(), e);
            String title = (request != null && request.title() != null) ? request.title() : "未知标题";
            return new GeneratePDFResponse(
                    title,
                    "",
                    0,
                    0,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    false,
                    "生成失败: " + e.getMessage()
            );
        }
    }
    
    /**
     * 执行计划PDF生成工具
     */
    @Tool(description = "生成任务执行计划的PDF报告，包含目标、步骤、结果等信息")
    public GeneratePDFResponse generateExecutionPlanPdf(GenerateExecutionPlanPDFRequest request) {
        try {
            log.info("生成执行计划PDF: {} - 步骤数: {}", 
                    request.title(), request.steps().size());
            
            // 构建执行计划内容
            StringBuilder content = new StringBuilder();
            content.append("# ").append(request.title()).append("\n\n");
            content.append("## 任务描述\n");
            content.append(request.taskDescription()).append("\n\n");
            content.append("## 执行步骤\n");
            
            for (int i = 0; i < request.steps().size(); i++) {
                content.append(String.format("%d. %s\n", i + 1, request.steps().get(i)));
            }
            
            content.append("\n## 预期结果\n");
            content.append(request.expectedResult()).append("\n");
            
            // 转换为PDF生成请求
            GeneratePDFRequest pdfRequest = new GeneratePDFRequest(
                    request.title(),
                    content.toString(),
                    "plan",
                    request.outputPath(),
                    Map.of("type", "execution_plan")
            );
            
            return generatePdfReport(pdfRequest);
            
        } catch (Exception e) {
            log.error("生成执行计划PDF失败: {}", e.getMessage(), e);
            return new GeneratePDFResponse(
                    request.title(),
                    "",
                    0,
                    0,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    false,
                    "生成执行计划PDF失败: " + e.getMessage()
            );
        }
    }
    
    /**
     * 会议纪要PDF生成工具
     */
    @Tool(description = "生成会议纪要PDF报告，包含参与者、内容、决议和行动项")
    public GeneratePDFResponse generateMeetingSummaryPdf(GenerateMeetingSummaryPDFRequest request) {
        try {
            log.info("生成会议纪要PDF: {} - 参与者: {} 人", 
                    request.meetingTitle(), request.participants().size());
            
            // 构建会议纪要内容
            StringBuilder content = new StringBuilder();
            content.append("# ").append(request.meetingTitle()).append("\n\n");
            content.append("**会议时间：** ").append(request.meetingTime()).append("\n\n");
            
            content.append("## 参与者\n");
            for (String participant : request.participants()) {
                content.append("- ").append(participant).append("\n");
            }
            
            content.append("\n## 会议内容\n");
            content.append(request.content()).append("\n\n");
            
            if (!request.decisions().isEmpty()) {
                content.append("## 关键决议\n");
                for (int i = 0; i < request.decisions().size(); i++) {
                    content.append(String.format("%d. %s\n", i + 1, request.decisions().get(i)));
                }
                content.append("\n");
            }
            
            if (!request.actionItems().isEmpty()) {
                content.append("## 行动项\n");
                for (int i = 0; i < request.actionItems().size(); i++) {
                    content.append(String.format("%d. %s\n", i + 1, request.actionItems().get(i)));
                }
                content.append("\n");
            }
            
            // 转换为PDF生成请求
            GeneratePDFRequest pdfRequest = new GeneratePDFRequest(
                    request.meetingTitle() + " - 会议纪要",
                    content.toString(),
                    "summary",
                    request.outputPath(),
                    Map.of("type", "meeting_summary", "meeting_time", request.meetingTime())
            );
            
            return generatePdfReport(pdfRequest);
            
        } catch (Exception e) {
            log.error("生成会议纪要PDF失败: {}", e.getMessage(), e);
            return new GeneratePDFResponse(
                    request.meetingTitle(),
                    "",
                    0,
                    0,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    false,
                    "生成会议纪要PDF失败: " + e.getMessage()
            );
        }
    }
    
    /**
     * 模拟PDF生成过程
     */
    private GeneratePDFResponse simulateGeneratePDF(GeneratePDFRequest request) {
        // 模拟生成过程
        String outputPath = request.outputPath() != null ? 
                request.outputPath() : "/tmp/reports/";
        
        String fileName = sanitizeFileName(request.title()) + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        String fullPath = outputPath + fileName;
        
        // 模拟文件大小和页数计算
        long fileSize = request.content().length() * 2; // 简单估算
        int pageCount = Math.max(1, request.content().length() / 2000); // 每页约2000字符
        
        return new GeneratePDFResponse(
                request.title(),
                fullPath,
                fileSize,
                pageCount,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                true,
                "PDF报告生成成功"
        );
    }
    
    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}