package com.onepiece.xmz.app.domain.repository;

import com.onepiece.xmz.app.domain.entity.AiRagOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI知识库配置Repository
 * 
 * @author SmartAI-Assistant
 */
@Repository
public interface AiRagOrderRepository extends JpaRepository<AiRagOrder, Long> {
    
    /**
     * 根据知识库名称查找
     */
    Optional<AiRagOrder> findByRagName(String ragName);
    
    /**
     * 根据知识标签查找
     */
    Optional<AiRagOrder> findByKnowledgeTag(String knowledgeTag);
    
    /**
     * 查找启用状态的知识库
     */
    List<AiRagOrder> findByStatus(Integer status);
    
    /**
     * 统计启用的知识库数量
     */
    @Query("SELECT COUNT(r) FROM AiRagOrder r WHERE r.status = 1")
    Long countActiveRagOrders();
    
    /**
     * 检查知识库名称是否存在
     */
    boolean existsByRagName(String ragName);
    
    /**
     * 检查知识标签是否存在
     */
    boolean existsByKnowledgeTag(String knowledgeTag);
}


