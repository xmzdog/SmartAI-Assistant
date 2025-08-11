package com.onepiece.xmz.app.domain.repository;

import com.onepiece.xmz.app.domain.entity.AiAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI智能体Repository
 * 
 * @author SmartAI-Assistant
 */
@Repository
public interface AiAgentRepository extends JpaRepository<AiAgent, Long> {
    
    /**
     * 根据智能体名称查找
     */
    Optional<AiAgent> findByAgentName(String agentName);
    
    /**
     * 查找启用状态的智能体
     */
    List<AiAgent> findByStatus(Integer status);
    
    /**
     * 根据渠道类型查找智能体
     */
    List<AiAgent> findByChannelAndStatus(String channel, Integer status);
    
    /**
     * 统计启用的智能体数量
     */
    @Query("SELECT COUNT(a) FROM AiAgent a WHERE a.status = 1")
    Long countActiveAgents();
    
    /**
     * 查找有任务调度的智能体
     */
    @Query("SELECT DISTINCT a FROM AiAgent a " +
           "JOIN AiAgentTaskSchedule ts ON a.id = ts.agentId " +
           "WHERE a.status = 1 AND ts.status = 1")
    List<AiAgent> findAgentsWithActiveSchedules();
}


