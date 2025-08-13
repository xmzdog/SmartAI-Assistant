package com.onepiece.trigger.http.admin;

import com.onepiece.infrastructure.dao.IAiAgentFlowConfigDao;
import com.onepiece.infrastructure.dao.po.AiAgentFlowConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/agent/flow/config/")
public class AiAdminAgentFlowConfigController {

    @Resource
    private IAiAgentFlowConfigDao aiAgentFlowConfigDao;

    @PostMapping("list")
    public ResponseEntity<List<Map<String, Object>>> list(@RequestBody(required = false) AiAgentFlowConfig query) {
        try {
            List<Map<String, Object>> configList;
            if (query != null && query.getAgentId() != null) {
                configList = aiAgentFlowConfigDao.queryByAgentIdWithModelName(String.valueOf(query.getAgentId()));
                log.info("按AgentId查询流程配置，AgentId: {}, 返回 {} 条记录", query.getAgentId(), configList.size());
            } else {
                configList = aiAgentFlowConfigDao.queryAllWithModelName();
                log.info("查询全部流程配置，返回 {} 条记录", configList.size());
            }
            
            return ResponseEntity.ok(configList);
        } catch (Exception e) {
            log.error("查询流程配置失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 测试方法：验证基础查询是否工作
     */
    @GetMapping("test")
    public ResponseEntity<Map<String, Object>> test() {
        try {
            Map<String, Object> result = new java.util.HashMap<>();
            
            // 1. 测试基础查询
            List<AiAgentFlowConfig> basicList = aiAgentFlowConfigDao.queryAll();
            result.put("basicQueryCount", basicList.size());
            result.put("basicQueryData", basicList);
            
            // 2. 测试带模型名的查询
            List<Map<String, Object>> withModelList = aiAgentFlowConfigDao.queryAllWithModelName();
            result.put("withModelQueryCount", withModelList.size());
            result.put("withModelQueryData", withModelList);
            
            log.info("测试结果 - 基础查询: {} 条, 带模型名查询: {} 条", basicList.size(), withModelList.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试查询失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("get")
    public ResponseEntity<AiAgentFlowConfig> get(@RequestParam("id") String id) {
        try {
            return ResponseEntity.ok(aiAgentFlowConfigDao.queryById(id));
        } catch (Exception e) {
            log.error("查询流程配置失败 id={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("add")
    public ResponseEntity<Boolean> add(@RequestBody AiAgentFlowConfig cfg) {
        try {
            cfg.setCreateTime(LocalDateTime.now());
            int c = aiAgentFlowConfigDao.insert(cfg);
            return ResponseEntity.ok(c > 0);
        } catch (Exception e) {
            log.error("新增流程配置失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("update")
    public ResponseEntity<Boolean> update(@RequestBody AiAgentFlowConfig cfg) {
        try {
            int c = aiAgentFlowConfigDao.updateById(cfg);
            return ResponseEntity.ok(c > 0);
        } catch (Exception e) {
            log.error("更新流程配置失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("delete")
    public ResponseEntity<Boolean> delete(@RequestParam("id") String id) {
        try {
            int c = aiAgentFlowConfigDao.deleteById(id);
            return ResponseEntity.ok(c > 0);
        } catch (Exception e) {
            log.error("删除流程配置失败 id={} ", id, e);
            return ResponseEntity.status(500).build();
        }
    }
}


