package com.onepiece.domain.agent.service.rag;

import com.onepiece.domain.agent.adapter.repository.IAgentRepository;
import com.onepiece.domain.agent.model.valobj.AiRagOrderVO;
import com.onepiece.domain.agent.service.IAiAgentRagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 知识库服务
 *
 *  
 * 2025-05-05 16:41
 */
@Slf4j
@Service
public class AiAgentRagService implements IAiAgentRagService {

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private PgVectorStore vectorStore;

    @Resource
    private IAgentRepository repository;

    @Override
    public void storeRagFile(String name, String tag, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documentList = tokenTextSplitter.apply(documentReader.get());

            // 添加知识库标签
            documentList.forEach(doc -> doc.getMetadata().put("knowledge", tag));

            // 存储知识库文件
            vectorStore.accept(documentList);

            // 存储到数据库
//            AiRagOrderVO aiRagOrderVO = new AiRagOrderVO();
//            aiRagOrderVO.setRagName(name);
//            aiRagOrderVO.setKnowledgeTag(tag);
//            repository.createTagOrder(aiRagOrderVO);
        }
    }

}
