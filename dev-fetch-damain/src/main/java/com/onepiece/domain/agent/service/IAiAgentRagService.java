package com.onepiece.domain.agent.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 知识库服务接口
 *  
 * 2025-05-05 16:40
 */
public interface IAiAgentRagService {

    void storeRagFile(String name, String tag, List<MultipartFile> files);

}
