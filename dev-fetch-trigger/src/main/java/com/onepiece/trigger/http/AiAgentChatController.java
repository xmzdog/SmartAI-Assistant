package com.onepiece.trigger.http;

import com.onepiece.api.response.Response;
import com.onepiece.domain.agent.model.entity.ChatHistoryEntity;
import com.onepiece.domain.agent.model.entity.ChatSessionEntity;
import com.onepiece.domain.agent.service.IAiChatHistoryService;
import com.onepiece.types.common.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天历史接口
 *
 * 负责对接前端 /api/v1/ai/agent/chat/* 路径
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/agent/chat/")
public class AiAgentChatController {

    @Resource
    private IAiChatHistoryService chatHistoryService;

    /**
     * 获取聊天历史列表
     */
    @GetMapping("history")
    public Response<List<ChatSessionEntity>> getChatHistory(@RequestParam("agentId") Long agentId,
                                                            @RequestParam(value = "ragId", required = false) Long ragId) {
        try {
            List<ChatSessionEntity> sessions = chatHistoryService.getChatHistory(agentId, ragId);
            return Response.<List<ChatSessionEntity>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(sessions)
                    .build();
        } catch (Exception e) {
            log.error("查询聊天历史失败, agentId={}, ragId={}", agentId, ragId, e);
            return Response.<List<ChatSessionEntity>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 保存聊天记录
     */
    @PostMapping("save")
    public Response<Boolean> saveChatRecord(@RequestBody ChatHistoryEntity chatHistory) {
        try {
            chatHistoryService.saveChatRecord(chatHistory);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("保存聊天记录失败, id={}", chatHistory != null ? chatHistory.getId() : null, e);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * 删除聊天记录
     */
    @DeleteMapping("{id}")
    public Response<Boolean> deleteChatRecord(@PathVariable("id") String sessionId) {
        try {
            chatHistoryService.deleteChatSession(sessionId);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("删除聊天记录失败, sessionId={}", sessionId, e);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }
}


