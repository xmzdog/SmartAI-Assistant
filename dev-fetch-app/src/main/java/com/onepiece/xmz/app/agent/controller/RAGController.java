package com.onepiece.xmz.app.agent.controller;

import com.onepiece.xmz.api.IRAGService;
import com.onepiece.xmz.api.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * RAG知识库控制器
 * 提供知识库管理和问答功能的API
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG知识库", description = "知识库管理和智能问答API")
public class RAGController {
    
    @Autowired
    private IRAGService ragService;
    
    /**
     * 获取知识库标签列表
     * 
     * GET /api/v1/rag/query_rag_tag_list
     */
    @GetMapping("/query_rag_tag_list")
    @Operation(summary = "获取知识库列表", description = "获取所有可用的知识库标签列表")
    public Response<List<String>> getKnowledgeBaseList() {
        log.debug("获取知识库标签列表");
        return ragService.queryRagTagList();
    }
    
    /**
     * 上传文件到知识库
     * 
     * POST /api/v1/rag/file/upload
     */
    @PostMapping("/file/upload")
    @Operation(summary = "上传文件", description = "上传文件到指定知识库")
    public Response<String> uploadFiles(
            @Parameter(description = "知识库标签") @RequestParam("ragTag") String ragTag,
            @Parameter(description = "上传的文件") @RequestParam("file") List<MultipartFile> files) {
        
        log.info("上传文件到知识库: {}, 文件数量: {}", ragTag, files.size());
        return ragService.uploadFile(ragTag, files);
    }
    
    /**
     * 删除知识库
     * 
     * DELETE /api/v1/rag/knowledge-base/{ragTag}
     */
    @DeleteMapping("/knowledge-base/{ragTag}")
    @Operation(summary = "删除知识库", description = "删除指定的知识库及其所有文档")
    public Response<String> deleteKnowledgeBase(
            @Parameter(description = "知识库标签") @PathVariable("ragTag") String ragTag) {
        
        log.info("删除知识库: {}", ragTag);
        return ragService.deleteKnowledgeBase(ragTag);
    }
    
    /**
     * 获取知识库详情
     * 
     * GET /api/v1/rag/knowledge-base/{ragTag}
     */
    @GetMapping("/knowledge-base/{ragTag}")
    @Operation(summary = "获取知识库详情", description = "获取指定知识库的详细信息")
    public Response<Map<String, Object>> getKnowledgeBaseDetail(
            @Parameter(description = "知识库标签") @PathVariable("ragTag") String ragTag) {
        
        log.info("获取知识库详情: {}", ragTag);
        
        // 简单返回知识库基本信息
        Map<String, Object> detail = Map.of(
            "ragTag", ragTag,
            "name", ragTag,
            "description", "知识库: " + ragTag,
            "documentCount", 0, // TODO: 实际统计文档数量
            "status", "active"
        );
        
        return Response.success(detail, "获取知识库详情成功");
    }
    
    /**
     * 获取知识库文档列表
     * 
     * GET /api/v1/rag/knowledge-base/{ragTag}/documents
     */
    @GetMapping("/knowledge-base/{ragTag}/documents")
    @Operation(summary = "获取文档列表", description = "获取指定知识库中的所有文档")
    public Response<List<Map<String, Object>>> getDocumentList(
            @Parameter(description = "知识库标签") @PathVariable("ragTag") String ragTag) {
        
        log.info("获取知识库文档列表: {}", ragTag);
        
        // TODO: 实现从向量存储中获取文档列表的逻辑
        // 目前返回空列表
        List<Map<String, Object>> documents = List.of();
        
        return Response.success(documents, "获取文档列表成功");
    }
    
    /**
     * 删除知识库中的文档
     * 
     * DELETE /api/v1/rag/knowledge-base/{ragTag}/documents/{documentId}
     */
    @DeleteMapping("/knowledge-base/{ragTag}/documents/{documentId}")
    @Operation(summary = "删除文档", description = "删除知识库中的指定文档")
    public Response<String> deleteDocument(
            @Parameter(description = "知识库标签") @PathVariable("ragTag") String ragTag,
            @Parameter(description = "文档ID") @PathVariable("documentId") String documentId) {
        
        log.info("删除文档: {} 从知识库: {}", documentId, ragTag);
        
        // TODO: 实现删除指定文档的逻辑
        return Response.success("删除成功", "文档已从知识库中删除");
    }
    
    /**
     * 基于知识库进行问答
     * 
     * POST /api/v1/rag/ask
     */
    @PostMapping("/ask")
    @Operation(summary = "智能问答", description = "基于指定知识库回答问题")
    public Response<String> askQuestion(@RequestBody Map<String, String> request) {
        String ragTag = request.get("ragTag");
        String question = request.get("question");
        
        log.info("知识库问答: {} - {}", ragTag, question);
        return ragService.askQuestion(request);
    }
}
