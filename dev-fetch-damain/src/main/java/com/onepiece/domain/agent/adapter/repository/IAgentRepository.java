package com.onepiece.domain.agent.adapter.repository;

import com.onepiece.domain.agent.model.valobj.*;

import java.util.List;
import java.util.Map;

/**
 * 仓储服务
 *  
 * 2025-05-02 14:15
 */
public interface IAgentRepository {

    List<AiClientModelVO> queryAiClientModelVOListByClientIds(List<Long> clientIdList);

    List<AiClientToolMcpVO> queryAiClientToolMcpVOListByClientIds(List<Long> clientIdList);

    List<AiClientAdvisorVO> queryAdvisorConfigByClientIds(List<Long> clientIdList);

    Map<Long, AiClientSystemPromptVO> querySystemPromptConfigByClientIds(List<Long> clientIdList);

    List<AiClientVO> queryAiClientByClientIds(List<Long> clientIdList);

    List<Long> queryAiClientIds();

    List<Long> queryAiClientIdsByAiAgentId(Long aiAgentId);

    List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();

    void createTagOrder(AiRagOrderVO aiRagOrderVO);

    String queryRagKnowledgeTag(Long ragId);

    Long queryAiClientModelIdByAgentId(Long aiAgentId);

    Map<String, AiAgentClientFlowConfigVO> queryAiAgentClientFlowConfig(String aiAgentId);


}
