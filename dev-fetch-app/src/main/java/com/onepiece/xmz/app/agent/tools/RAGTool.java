package com.onepiece.xmz.app.agent.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG知识库检索工具
 * 基于SpringAI Tools机制
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class RAGTool {
    
    @Autowired
    @Qualifier("pgVectorStore")
    private VectorStore vectorStore;
    
    /**
     * RAG搜索请求
     */
    @JsonClassDescription("RAG语义搜索请求")
    public static class RAGSearchRequest {
        @JsonPropertyDescription("搜索查询文本")
        private String query;
        @JsonPropertyDescription("返回结果数量，默认5")
        private Integer topK;
        @JsonPropertyDescription("相似度阈值，默认0.7")
        private Double threshold;
        @JsonPropertyDescription("知识库名称，可选")
        private String knowledgeBase;

        public RAGSearchRequest() {}

        public RAGSearchRequest(String query, Integer topK, Double threshold, String knowledgeBase) {
            this.query = query;
            this.topK = topK;
            this.threshold = threshold;
            this.knowledgeBase = knowledgeBase;
        }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }

        public Double getThreshold() { return threshold; }
        public void setThreshold(Double threshold) { this.threshold = threshold; }

        public String getKnowledgeBase() { return knowledgeBase; }
        public void setKnowledgeBase(String knowledgeBase) { this.knowledgeBase = knowledgeBase; }

        // 为了兼容record的访问方式，保留这些方法
        public String query() { return query; }
        public Integer topK() { return topK; }
        public Double threshold() { return threshold; }
        public String knowledgeBase() { return knowledgeBase; }
    }
    
    /**
     * RAG搜索响应
     */
    public static class RAGSearchResponse {
        private String query;
        private int resultCount;
        private List<String> documents;
        private double maxSimilarity;

        public RAGSearchResponse() {}

        public RAGSearchResponse(String query, int resultCount, List<String> documents, double maxSimilarity) {
            this.query = query;
            this.resultCount = resultCount;
            this.documents = documents;
            this.maxSimilarity = maxSimilarity;
        }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public int getResultCount() { return resultCount; }
        public void setResultCount(int resultCount) { this.resultCount = resultCount; }

        public List<String> getDocuments() { return documents; }
        public void setDocuments(List<String> documents) { this.documents = documents; }

        public double getMaxSimilarity() { return maxSimilarity; }
        public void setMaxSimilarity(double maxSimilarity) { this.maxSimilarity = maxSimilarity; }

        // 为了兼容record的访问方式，保留这些方法
        public String query() { return query; }
        public int resultCount() { return resultCount; }
        public List<String> documents() { return documents; }
        public double maxSimilarity() { return maxSimilarity; }
    }
    
    /**
     * RAG问答请求
     */
    @JsonClassDescription("RAG知识问答请求")
    public static class RAGQARequest {
        @JsonPropertyDescription("用户问题")
        private String question;
        @JsonPropertyDescription("上下文文档数量，默认3")
        private Integer contextCount;
        @JsonPropertyDescription("知识库名称，可选")
        private String knowledgeBase;

        public RAGQARequest() {}

        public RAGQARequest(String question, Integer contextCount, String knowledgeBase) {
            this.question = question;
            this.contextCount = contextCount;
            this.knowledgeBase = knowledgeBase;
        }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public Integer getContextCount() { return contextCount; }
        public void setContextCount(Integer contextCount) { this.contextCount = contextCount; }

        public String getKnowledgeBase() { return knowledgeBase; }
        public void setKnowledgeBase(String knowledgeBase) { this.knowledgeBase = knowledgeBase; }

        // 为了兼容record的访问方式，保留这些方法
        public String question() { return question; }
        public Integer contextCount() { return contextCount; }
        public String knowledgeBase() { return knowledgeBase; }
    }
    
    /**
     * RAG问答响应
     */
    public static class RAGQAResponse {
        private String question;
        private String answer;
        private List<String> sources;
        private double confidence;

        public RAGQAResponse() {}

        public RAGQAResponse(String question, String answer, List<String> sources, double confidence) {
            this.question = question;
            this.answer = answer;
            this.sources = sources;
            this.confidence = confidence;
        }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public List<String> getSources() { return sources; }
        public void setSources(List<String> sources) { this.sources = sources; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        // 为了兼容record的访问方式，保留这些方法
        public String question() { return question; }
        public String answer() { return answer; }
        public List<String> sources() { return sources; }
        public double confidence() { return confidence; }
    }
    
    /**
     * RAG语义搜索工具
     */
    @Tool(description = "在知识库中进行语义搜索，返回相关文档片段")
    public RAGSearchResponse ragSearch(RAGSearchRequest request) {
        try {
            log.info("执行RAG搜索: {} (topK={}, threshold={})", 
                    request.query(), request.topK(), request.threshold());
            
            int topK = request.topK() != null ? request.topK() : 5;
            double threshold = request.threshold() != null ? request.threshold() : 0.7;

            SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
                    .query(request.query())
                    .topK(topK)
                    .similarityThreshold(threshold);
            
            // 如果指定了知识库，添加过滤条件
            if (request.knowledgeBase() != null && !request.knowledgeBase().isEmpty()) {
                searchRequestBuilder.filterExpression("knowledge_base == '" + request.knowledgeBase() + "'");
                log.info("添加知识库过滤条件: knowledge_base == '{}'", request.knowledgeBase());
            }
            
            SearchRequest searchRequest = searchRequestBuilder.build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            List<String> documents = results.stream()
                    .map(doc -> doc.getText())
                    .collect(Collectors.toList());

            double maxSimilarity = results.stream()
                    .mapToDouble(doc -> {
                        Object scoreObj = doc.getMetadata().get("distance");
                        if (scoreObj instanceof Number) {
                            double distance = ((Number) scoreObj).doubleValue();
                            return 1.0 - distance; // 转换成相似度
                        }
                        return 0.0;
                    })
                    .max()
                    .orElse(0.0);

//            double maxSimilarity = results.stream()
//                    .mapToDouble(doc -> {
//                        Float score = doc.getMetadata().get("distance", Float.class);
//                        return score != null ? (1.0 - score) : 0.0;
//                    })
//                    .max()
//                    .orElse(0.0);
            
            log.info("RAG搜索完成，找到 {} 个相关文档", results.size());
            
            return new RAGSearchResponse(
                    request.query(),
                    results.size(),
                    documents,
                    maxSimilarity
            );
            
        } catch (Exception e) {
            log.error("RAG搜索失败: {}", e.getMessage(), e);
            return new RAGSearchResponse(
                    request.query(),
                    0,
                    Arrays.asList("搜索失败: " + e.getMessage()),
                    0.0
            );
        }
    }
    
    /**
     * RAG问答工具
     */
    @Tool(description = "基于知识库内容回答问题，提供上下文相关的准确答案")
    public RAGQAResponse ragQA(RAGQARequest request) {
        try {
            log.info("执行RAG问答: {} (contextCount={})", 
                    request.question(), request.contextCount());
            
            int contextCount = request.contextCount() != null ? request.contextCount() : 3;
            
            // 先进行搜索获取相关文档
            RAGSearchRequest searchRequest = new RAGSearchRequest(
                    request.question(), 
                    contextCount, 
                    0.7, 
                    request.knowledgeBase()
            );
            
            RAGSearchResponse searchResponse = ragSearch(searchRequest);
            
            if (searchResponse.documents().isEmpty()) {
                return new RAGQAResponse(
                        request.question(),
                        "抱歉，没有找到相关信息来回答您的问题。",
                        Collections.emptyList(),
                        0.0
                );
            }
            
            // 基于搜索到的文档生成答案
            String context = String.join("\n\n", searchResponse.documents());
            String answer = String.format("基于知识库内容，%s\n\n相关信息：\n%s", 
                    generateAnswerFromContext(request.question(), context),
                    context.length() > 500 ? context.substring(0, 500) + "..." : context
            );
            
            log.info("RAG问答完成，置信度: {}", searchResponse.maxSimilarity());
            
            return new RAGQAResponse(
                    request.question(),
                    answer,
                    searchResponse.documents(),
                    searchResponse.maxSimilarity()
            );
            
        } catch (Exception e) {
            log.error("RAG问答失败: {}", e.getMessage(), e);
            return new RAGQAResponse(
                    request.question(),
                    "问答失败: " + e.getMessage(),
                    Collections.emptyList(),
                    0.0
            );
        }
    }
    
    /**
     * 基于上下文生成答案（简化实现）
     */
    private String generateAnswerFromContext(String question, String context) {
        // 这里可以集成更复杂的答案生成逻辑
        // 目前返回简化的响应
        return "根据相关文档内容整理的回答";
    }
}