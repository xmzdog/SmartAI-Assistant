package com.onepiece.domain.agent.service.armory.node;

import com.onepiece.domain.agent.model.entity.AiAgentEngineStarterEntity;
import com.onepiece.domain.agent.model.valobj.*;
import com.onepiece.domain.agent.service.armory.AbstractArmorySupport;
import com.onepiece.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-02 13:46
 */
@Slf4j
@Component
public class RootNode extends AbstractArmorySupport {

    @Resource
    private AiClientToolMcpNode aiClientToolMcpNode;

    @Override
    protected void multiThread(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<AiClientModelVO>> aiClientModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", requestParameter.getClientIdList());
            return repository.queryAiClientModelVOListByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientToolMcpVO>> aiClientToolMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) {}", requestParameter.getClientIdList());
            return repository.queryAiClientToolMcpVOListByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientAdvisorVO>> aiClientAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", requestParameter.getClientIdList());
            return repository.queryAdvisorConfigByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture<Map<Long, AiClientSystemPromptVO>> aiSystemPromptConfigFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_system_prompt) {}", requestParameter.getClientIdList());
            return repository.querySystemPromptConfigByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientVO>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", requestParameter.getClientIdList());
            return repository.queryAiClientByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture.allOf(aiClientModelListFuture)
                .thenRun(() -> {
                    dynamicContext.setValue("aiClientModelList", aiClientModelListFuture.join());
                    dynamicContext.setValue("aiClientToolMcpList", aiClientToolMcpListFuture.join());
                    dynamicContext.setValue("aiClientAdvisorList", aiClientAdvisorListFuture.join());
                    dynamicContext.setValue("aiSystemPromptConfig", aiSystemPromptConfigFuture.join());
                    dynamicContext.setValue("aiClientList", aiClientListFuture.join());
                }).join();
    }

    @Override
    protected String doApply(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，数据加载节点 {}", JSON.toJSONString(requestParameter));
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientToolMcpNode;
    }

}
