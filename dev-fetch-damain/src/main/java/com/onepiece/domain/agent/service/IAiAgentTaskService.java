package com.onepiece.domain.agent.service;

import com.onepiece.domain.agent.model.valobj.AiAgentTaskScheduleVO;

import java.util.List;

/**
 * 智能体任务服务
 *
 *  
 * 2025-05-05 15:20
 */
public interface IAiAgentTaskService {

    List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();

}
