package com.onepiece.xmz.app.config;



import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OllamaConfig {

    /**
     *  ollamaApi 是与Ollama 本地或远程服务交互的Java 客户端
     * @param baseUrl
     * @return
     */
    @Bean
    public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url}") String baseUrl) {
        return OllamaApi.builder().baseUrl(baseUrl).build();
    }

    /**
     * 这里使用defaultOptions 给 OllamaChatModel 设置默认的参数
     * .model 使用本地安装的模型
     * .temperature 设置这个对话模型的“随机度”参数，或者叫温度值，数值越高，模型回答越发散、越有创意；越低越严谨保守；
     * @param ollamaApi
     * @return
     */
    @Bean
    public OllamaChatModel ollamaChatModelByDeepseek(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder()
                        .model("deepseek-r1:1.5b")     // 模型
                        .temperature(0.7)
                        .build())
                .build();
    }

    /**
     * 这个在使用时设置模型对象和参数
     * @param ollamaApi
     * @return
     */
    @Bean
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .build();
    }


    /**
     * TokenTextSplitter = 自动文本分块器
     *
     * 长文本 → 分块 → 向量数据库 / RAG / LLM 输入
     *
     * 解决 token 限制 + 提高语义检索精度
     * @return
     * 将长文本按“token”长度拆分为多个小块，用于向量化（Embedding）或 RAG 检索时避免文本过长导致模型处理超限。
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder().build();
    }

    /**
     * 👉 基于内存的简单向量存储，用于存储文档向量（embeddings）并支持向量检索。方便快速开发、调试，但不适合生产环境。
     * @param embeddingModel
     * @return
     */
    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel)
                .build();
    }

    /**
     *  👉 基于 PostgreSQL + pgvector 插件 的向量数据库实现，用于 持久化存储向量数据 并进行高效的相似度检索。
     * @param embeddingModel
     * @param jdbcTemplate
     * @return
     *
     * PgVectorStore = 生产级向量数据库解决方案，基于 Postgres + pgvector，持久化、可扩展、适合大规模知识库场景。
     *
     *  新增数据：add() 把文本封装为 Document → 使用 EmbeddingModel 生成向量 → 插入 PostgreSQL 的 pgvector 扩展表。
     *
     * 检索：similaritySearch() 对查询问题生成向量 → 调用 pgvector 的 <-> 余弦相似度 / L2 距离检索。
     */
    @Bean
    public PgVectorStore pgVectorStore(EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
    }
}
