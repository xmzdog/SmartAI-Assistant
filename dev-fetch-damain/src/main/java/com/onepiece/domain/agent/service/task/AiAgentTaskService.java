package com.onepiece.domain.agent.service.task;

import com.onepiece.domain.agent.adapter.repository.IAgentRepository;
import com.onepiece.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import com.onepiece.domain.agent.service.IAiAgentTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体执行任务
 *  
 * 2025-05-05 15:21
 */
@Service
public class AiAgentTaskService implements IAiAgentTaskService {

    @Resource
    private IAgentRepository repository;

    @Override
    public List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule() {
        return repository.queryAllValidTaskSchedule();
    }

    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return repository.queryAllInvalidTaskScheduleIds();
    }

}
