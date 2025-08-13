package com.onepiece.domain.agent.service.armory.factory;

import com.onepiece.domain.agent.model.entity.AiAgentEngineStarterEntity;
import com.onepiece.domain.agent.model.entity.ArmoryCommandEntity;
import com.onepiece.domain.agent.service.armory.node.RootNode;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 工厂类
 *
 *  
 * 2025-05-02 13:24
 */
@Service
public class DefaultArmoryStrategyFactory {

    @Resource
    private ApplicationContext applicationContext;

    private final RootNode rootNode;

//    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler(){
//        return rootNode;
//    }

    public DefaultArmoryStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<AiAgentEngineStarterEntity, DynamicContext, String> strategyHandler() {
        return rootNode;
    }

    public ChatClient chatClient(Long clientId) {
        return (ChatClient) applicationContext.getBean("ai_client_" + clientId);
    }

    public ChatModel chatModel(Long modelId) {
        return (ChatModel) applicationContext.getBean("AiClientModel_" + modelId);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private int level;

        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

    }

}
