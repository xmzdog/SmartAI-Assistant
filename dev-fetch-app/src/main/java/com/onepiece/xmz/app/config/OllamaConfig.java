package com.onepiece.xmz.app.config;



import io.micrometer.observation.ObservationRegistry;

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


//    @Bean
//    public ToolCallingManager toolCallingManager() {
//        return new ToolCallingManager();  // 你可能需要自定义或替换默认管理器
//    }
//
//    @Bean
//    public ObservationRegistry observationRegistry() {
//        return new ObservationRegistry();  // 同样，如果有需要，可以自定义
//    }
//
//    @Bean
//    public ModelManagementOptions modelManagementOptions() {
//        return new ModelManagementOptions();  // 提供适当的配置
//    }
//
//    @Bean
//    public OllamaOptions defaultOptions() {
//        return new OllamaOptions();  // 提供适当的配置
//    }
//
//    @Bean
//    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi, OllamaOptions defaultOptions,
//                                           ToolCallingManager toolCallingManager, ObservationRegistry observationRegistry,
//                                           ModelManagementOptions modelManagementOptions) {
//        return new OllamaChatModel(ollamaApi, defaultOptions, toolCallingManager, observationRegistry,
//                modelManagementOptions);
//    }
//
//    @Bean
//    public TokenTextSplitter tokenTextSplitter() {
//        return new TokenTextSplitter();
//    }
//
//    @Bean
//    public SimpleVectorStore simpleVectorStore(OllamaChatModel ollamaChatModel) {
//        return new SimpleVectorStore(ollamaChatModel);
//    }
//
//    @Bean
//    public PgVectorStore pgVectorStore(OllamaChatModel ollamaChatModel, JdbcTemplate jdbcTemplate) {
//        return new PgVectorStore(jdbcTemplate, ollamaChatModel);
//    }
}
