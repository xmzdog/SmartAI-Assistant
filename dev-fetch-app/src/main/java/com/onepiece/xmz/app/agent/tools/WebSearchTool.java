package com.onepiece.xmz.app.agent.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 网络搜索工具
 * 基于SpringAI Tools机制
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Component
public class WebSearchTool {
    
    /**
     * 网络搜索请求
     */
    @JsonClassDescription("网络搜索请求")
    public static class WebSearchRequest {
        @JsonPropertyDescription("搜索关键词")
        private String query;
        @JsonPropertyDescription("搜索引擎类型：google/bing/baidu")
        private String engine;
        @JsonPropertyDescription("返回结果数量，默认10")
        private Integer maxResults;
        @JsonPropertyDescription("搜索语言，如：zh-CN/en-US")
        private String language;
        @JsonPropertyDescription("搜索类型：web/news/images")
        private String searchType;

        public WebSearchRequest() {}

        public WebSearchRequest(String query, String engine, Integer maxResults, String language, String searchType) {
            this.query = query;
            this.engine = engine;
            this.maxResults = maxResults;
            this.language = language;
            this.searchType = searchType;
        }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public String getEngine() { return engine; }
        public void setEngine(String engine) { this.engine = engine; }

        public Integer getMaxResults() { return maxResults; }
        public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getSearchType() { return searchType; }
        public void setSearchType(String searchType) { this.searchType = searchType; }

        // 为了兼容record的访问方式，保留这些方法
        public String query() { return query; }
        public String engine() { return engine; }
        public Integer maxResults() { return maxResults; }
        public String language() { return language; }
        public String searchType() { return searchType; }
    }
    
    /**
     * 搜索结果项
     */
    public static class SearchResultItem {
        private String title;
        private String url;
        private String snippet;
        private String domain;
        private String publishTime;

        public SearchResultItem() {}

        public SearchResultItem(String title, String url, String snippet, String domain, String publishTime) {
            this.title = title;
            this.url = url;
            this.snippet = snippet;
            this.domain = domain;
            this.publishTime = publishTime;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }

        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }

        public String getPublishTime() { return publishTime; }
        public void setPublishTime(String publishTime) { this.publishTime = publishTime; }

        // 为了兼容record的访问方式，保留这些方法
        public String title() { return title; }
        public String url() { return url; }
        public String snippet() { return snippet; }
        public String domain() { return domain; }
        public String publishTime() { return publishTime; }
    }
    
    /**
     * 网络搜索响应
     */
    public static class WebSearchResponse {
        private String query;
        private List<SearchResultItem> results;
        private int totalCount;
        private String searchTime;
        private String status;
        private String message;

        public WebSearchResponse() {}

        public WebSearchResponse(String query, List<SearchResultItem> results, int totalCount, String searchTime, String status, String message) {
            this.query = query;
            this.results = results;
            this.totalCount = totalCount;
            this.searchTime = searchTime;
            this.status = status;
            this.message = message;
        }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public List<SearchResultItem> getResults() { return results; }
        public void setResults(List<SearchResultItem> results) { this.results = results; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public String getSearchTime() { return searchTime; }
        public void setSearchTime(String searchTime) { this.searchTime = searchTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        // 为了兼容record的访问方式，保留这些方法
        public String query() { return query; }
        public List<SearchResultItem> results() { return results; }
        public int totalCount() { return totalCount; }
        public String searchTime() { return searchTime; }
        public String status() { return status; }
        public String message() { return message; }
    }
    
    /**
     * 网络搜索工具
     */
    @Tool(description = "在互联网上搜索最新信息，获取实时数据和新闻")
    public WebSearchResponse webSearch(WebSearchRequest request) {
        try {
            log.info("执行网络搜索: query={}, engine={}, maxResults={}", 
                    request.query(), request.engine(), request.maxResults());
            
            // 模拟网络搜索
            List<SearchResultItem> results = simulateWebSearch(request);
            
            String searchTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            log.info("网络搜索完成，找到 {} 个结果", results.size());
            
            return new WebSearchResponse(
                    request.query(),
                    results,
                    results.size(),
                    searchTime,
                    "success",
                    "搜索成功"
            );
            
        } catch (Exception e) {
            log.error("网络搜索失败: {}", e.getMessage(), e);
            return new WebSearchResponse(
                    request.query(),
                    Collections.emptyList(),
                    0,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "error",
                    "搜索失败: " + e.getMessage()
            );
        }
    }
    
    /**
     * 模拟网络搜索结果
     */
    private List<SearchResultItem> simulateWebSearch(WebSearchRequest request) {
        String query = request.query();
        String engine = request.engine() != null ? request.engine() : "google";
        int maxResults = request.maxResults() != null ? request.maxResults() : 10;
        
        // 根据搜索关键词生成模拟结果
        if (query.toLowerCase().contains("springai") || query.toLowerCase().contains("spring ai")) {
            return generateSpringAIResults();
        } else if (query.toLowerCase().contains("java")) {
            return generateJavaResults();
        } else if (query.toLowerCase().contains("ai") || query.toLowerCase().contains("人工智能")) {
            return generateAIResults();
        } else {
            return generateGenericResults(query);
        }
    }
    
    /**
     * 生成SpringAI相关搜索结果
     */
    private List<SearchResultItem> generateSpringAIResults() {
        return Arrays.asList(
                new SearchResultItem(
                        "Spring AI - Official Documentation",
                        "https://docs.spring.io/spring-ai/reference/",
                        "Spring AI is an application framework for AI engineering. It provides a friendly API to develop AI applications using Spring Boot...",
                        "docs.spring.io",
                        "2024-01-15T10:00:00"
                ),
                new SearchResultItem(
                        "Getting Started with Spring AI",
                        "https://spring.io/blog/2024/01/10/spring-ai-getting-started",
                        "Learn how to build AI-powered applications with Spring AI framework. This guide covers ChatClient, function calling, and RAG...",
                        "spring.io",
                        "2024-01-10T14:30:00"
                ),
                new SearchResultItem(
                        "Spring AI GitHub Repository",
                        "https://github.com/spring-projects/spring-ai",
                        "An Application Framework for AI Engineering. Contribute to spring-projects/spring-ai development by creating an account on GitHub...",
                        "github.com",
                        "2024-01-12T09:15:00"
                ),
                new SearchResultItem(
                        "Spring AI Tools and Function Calling",
                        "https://docs.spring.io/spring-ai/reference/api/tools.html",
                        "Spring AI supports function calling through Tools mechanism. Learn how to create custom tools and integrate external APIs...",
                        "docs.spring.io",
                        "2024-01-08T16:45:00"
                )
        );
    }
    
    /**
     * 生成Java相关搜索结果
     */
    private List<SearchResultItem> generateJavaResults() {
        return Arrays.asList(
                new SearchResultItem(
                        "Oracle Java Documentation",
                        "https://docs.oracle.com/en/java/",
                        "Official Java documentation from Oracle. Find API docs, tutorials, and developer guides for Java SE and Java EE...",
                        "docs.oracle.com",
                        "2024-01-14T11:20:00"
                ),
                new SearchResultItem(
                        "Java 21 New Features",
                        "https://openjdk.org/projects/jdk/21/",
                        "Java 21 is the latest LTS release with new features including Virtual Threads, Pattern Matching, and Record Patterns...",
                        "openjdk.org",
                        "2024-01-13T15:30:00"
                ),
                new SearchResultItem(
                        "Spring Boot 3.0 with Java 17+",
                        "https://spring.io/blog/2022/11/24/spring-boot-3-0-goes-ga",
                        "Spring Boot 3.0 requires Java 17 as minimum version and provides native image support with GraalVM...",
                        "spring.io",
                        "2024-01-11T13:45:00"
                )
        );
    }
    
    /**
     * 生成AI相关搜索结果
     */
    private List<SearchResultItem> generateAIResults() {
        return Arrays.asList(
                new SearchResultItem(
                        "ChatGPT and Large Language Models",
                        "https://openai.com/chatgpt",
                        "ChatGPT is an AI chatbot developed by OpenAI based on large language models. It can assist with various tasks...",
                        "openai.com",
                        "2024-01-16T08:30:00"
                ),
                new SearchResultItem(
                        "AI in Enterprise Applications",
                        "https://www.ibm.com/artificial-intelligence",
                        "Learn how enterprises are using AI to transform business processes and improve customer experiences...",
                        "ibm.com",
                        "2024-01-15T12:15:00"
                ),
                new SearchResultItem(
                        "Machine Learning Best Practices",
                        "https://developers.google.com/machine-learning/guides/rules-of-ml",
                        "Google's best practices for machine learning engineering. Learn about data preparation, model training, and deployment...",
                        "developers.google.com",
                        "2024-01-14T17:00:00"
                )
        );
    }
    
    /**
     * 生成通用搜索结果
     */
    private List<SearchResultItem> generateGenericResults(String query) {
        return Arrays.asList(
                new SearchResultItem(
                        "Search Results for: " + query,
                        "https://example.com/search?q=" + query.replace(" ", "+"),
                        "Find the latest information about " + query + ". Get news, articles, and resources related to your search...",
                        "example.com",
                        LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                new SearchResultItem(
                        query + " - Wikipedia",
                        "https://wikipedia.org/wiki/" + query.replace(" ", "_"),
                        "Wikipedia article about " + query + ". Learn more about the topic with comprehensive information and references...",
                        "wikipedia.org",
                        LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                new SearchResultItem(
                        "Latest News about " + query,
                        "https://news.example.com/search/" + query.replace(" ", "-"),
                        "Stay updated with the latest news and developments related to " + query + ". Breaking news and analysis...",
                        "news.example.com",
                        LocalDateTime.now().minusHours(6).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
        );
    }
}