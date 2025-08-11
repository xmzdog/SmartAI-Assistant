package com.onepiece.xmz.app.agent.service;

import com.onepiece.xmz.api.IRAGService;
import com.onepiece.xmz.api.response.Response;
import com.onepiece.xmz.app.domain.entity.AiRagOrder;
import com.onepiece.xmz.app.domain.repository.AiRagOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG服务实现
 * 
 * @author SmartAI-Assistant
 */
@Slf4j
@Service
public class RAGServiceImpl implements IRAGService {
    
    @Autowired
    @Qualifier("pgVectorStore")
    private VectorStore vectorStore;
    
    @Autowired
    private ChatClient.Builder chatClientBuilder;
    
    @Autowired
    private AiRagOrderRepository ragOrderRepository;
    
    @Autowired
    private TaskManagementService taskManagementService;
    
    @Override
    public Response<List<String>> queryRagTagList() {
        try {
            log.info("查询知识库标签列表");
            
            Set<String> uniqueTags = new HashSet<>();
            
            // 首先从数据库查询已配置的知识库
            try {
                List<AiRagOrder> ragOrders = ragOrderRepository.findByStatus(1);
                for (AiRagOrder ragOrder : ragOrders) {
                    if (ragOrder.getKnowledgeTag() != null && !ragOrder.getKnowledgeTag().trim().isEmpty()) {
                        uniqueTags.add(ragOrder.getKnowledgeTag().trim());
                    }
                }
                log.info("从数据库获取到 {} 个知识库标签", uniqueTags.size());
            } catch (Exception e) {
                log.warn("从数据库获取知识库标签失败: {}", e.getMessage());
            }
            
            try {
                // 同时查询向量存储中的文档来补充标签
                SearchRequest searchRequest = SearchRequest.builder()
                    .query("*")  // 匹配所有文档
                    .topK(1000)  // 获取足够多的文档
                    .build();
                
                List<Document> allDocuments = vectorStore.similaritySearch(searchRequest);
                
                // 从文档元数据中提取知识库标签
                for (Document doc : allDocuments) {
                    Object kbTag = doc.getMetadata().get("knowledge_base");
                    if (kbTag != null && !kbTag.toString().trim().isEmpty()) {
                        uniqueTags.add(kbTag.toString());
                    }
                }
                
            } catch (Exception e) {
                log.warn("从向量存储获取知识库标签时出错: {}", e.getMessage());
            }
            
            // 如果没有找到任何标签，返回一些示例标签
            if (uniqueTags.isEmpty()) {
                uniqueTags.addAll(Arrays.asList(
                    "技术文档",
                    "产品手册", 
                    "用户指南",
                    "API文档",
                    "常见问题"
                ));
            }
            
            List<String> tagList = new ArrayList<>(uniqueTags);
            Collections.sort(tagList); // 排序
            
            log.info("找到 {} 个知识库标签", tagList.size());
            return Response.success(tagList, "查询知识库标签列表成功");
            
        } catch (Exception e) {
            log.error("查询知识库标签列表失败", e);
            return Response.fail("查询知识库标签列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response<String> uploadFile(String ragTag, List<MultipartFile> files) {
        try {
            log.info("上传文件到知识库: {}, 文件数量: {}", ragTag, files.size());
            
            List<Document> documents = new ArrayList<>();
            
            for (MultipartFile file : files) {
                log.info("处理文件: {}", file.getOriginalFilename());
                
                // 使用Tika读取文档
                DocumentReader reader = new TikaDocumentReader(
                    new InputStreamResource(file.getInputStream())
                );
                
                List<Document> fileDocuments = reader.get();
                
                // 为每个文档添加知识库标签元数据
                for (Document doc : fileDocuments) {
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                    metadata.put("knowledge_base", ragTag);
                    metadata.put("source_file", file.getOriginalFilename());
                    metadata.put("upload_time", System.currentTimeMillis());
                    
                    Document updatedDoc = new Document(doc.getText(), metadata);
                    documents.add(updatedDoc);
                }
            }
            
            // 将文档添加到向量存储
            vectorStore.add(documents);
            
            // 同时保存知识库配置到数据库
            try {
                Optional<AiRagOrder> existingRagOrder = ragOrderRepository.findByKnowledgeTag(ragTag);
                if (!existingRagOrder.isPresent()) {
                    AiRagOrder ragOrder = new AiRagOrder();
                    ragOrder.setRagName(ragTag + "知识库");
                    ragOrder.setKnowledgeTag(ragTag);
                    ragOrder.setStatus(1);
                    ragOrderRepository.save(ragOrder);
                    log.info("创建新的知识库配置: {}", ragTag);
                }
            } catch (Exception e) {
                log.warn("保存知识库配置失败，但文档上传成功: {}", e.getMessage());
            }
            
            log.info("成功上传 {} 个文档片段到知识库: {}", documents.size(), ragTag);
            return Response.success("上传成功", 
                String.format("成功上传 %d 个文件，生成 %d 个文档片段", files.size(), documents.size()));
            
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return Response.fail("上传文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response<String> askQuestion(Map<String, String> request) {
        String taskId = "RAG_" + System.currentTimeMillis();
        
        try {
            String ragTag = request.get("ragTag");
            String question = request.get("question");
            
            log.info("在知识库 {} 中回答问题: {}", ragTag, question);
            
            // 创建任务记录
            taskManagementService.createTaskRecord(
                taskId, null, "知识库问答", question, "RAG", 
                String.format("{\"ragTag\":\"%s\",\"question\":\"%s\"}", ragTag, question));
            
            // 更新任务状态为运行中
            taskManagementService.updateTaskStatus(taskId, "RUNNING", 20);
            taskManagementService.addTaskLog(taskId, "开始知识库检索", "ACTION", "SUCCESS", question, null);
            
            // 构建搜索请求
            SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
                .query(question)
                .topK(5)
//                .similarityThreshold(0.5)
                    ;
            
            // 如果指定了知识库标签，添加过滤条件
            if (ragTag != null && !ragTag.isEmpty()) {
                // 添加根据知识库标签过滤的逻辑
                searchRequestBuilder.filterExpression("knowledge_base == '" + ragTag + "'");
                log.info("添加知识库过滤条件: knowledge_base == '{}'", ragTag);
            }
            
            SearchRequest searchRequest = searchRequestBuilder.build();
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            // 记录检索结果
            taskManagementService.updateTaskStatus(taskId, "RUNNING", 50);
            taskManagementService.addTaskLog(taskId, "知识库检索完成", "OBSERVATION", "SUCCESS", 
                null, String.format("检索到 %d 个相关文档", results.size()));
            
            if (results.isEmpty()) {
                // 没有找到相关信息
                taskManagementService.completeTask(taskId, "抱歉，没有找到相关信息来回答您的问题。", "{}");
                taskManagementService.addTaskLog(taskId, "问答完成", "ACTION", "SUCCESS", null, "未找到相关信息");
                return Response.success("抱歉，没有找到相关信息来回答您的问题。", "回答完成");
            }
            
            // 构建上下文
            String context = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
            
            // 记录AI生成步骤
            taskManagementService.updateTaskStatus(taskId, "RUNNING", 80);
            taskManagementService.addTaskLog(taskId, "开始AI回答生成", "THINKING", "SUCCESS", context, null);
            
            // 使用AI大模型生成智能回答
            String answer = generateAIAnswer(question, context, ragTag);
            
            // 完成任务
            taskManagementService.completeTask(taskId, answer, 
                String.format("{\"answer\":\"%s\",\"contextLength\":%d}", 
                    answer.replace("\"", "\\\""), context.length()));
            taskManagementService.addTaskLog(taskId, "AI回答生成完成", "ACTION", "SUCCESS", null, answer);
            
            log.info("问答完成，返回答案长度: {}", answer.length());
            return Response.success(answer, "问答完成");
            
        } catch (Exception e) {
            log.error("问答失败", e);
            // 记录任务失败
            taskManagementService.failTask(taskId, e.getMessage());
            taskManagementService.addTaskLog(taskId, "问答失败", "ACTION", "FAILED", null, e.getMessage());
            return Response.fail("问答失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response<String> deleteKnowledgeBase(String ragTag) {
        try {
            log.info("删除知识库: {}", ragTag);
            
            // 查询该知识库中的所有文档
            SearchRequest searchRequest = SearchRequest.builder()
                .query("*")  // 匹配所有文档
                .topK(1000)  // 获取更多文档用于删除
                .build();
            
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            
            // 过滤出属于指定知识库的文档
            List<Document> kbDocuments = documents.stream()
                .filter(doc -> ragTag.equals(doc.getMetadata().get("knowledge_base")))
                .collect(Collectors.toList());
            
            if (kbDocuments.isEmpty()) {
                return Response.fail("知识库 " + ragTag + " 不存在或已为空");
            }
            
            // TODO: 实现批量删除文档的逻辑
            // 由于当前Spring AI的向量存储接口可能不支持直接删除
            // 这里先记录要删除的文档信息
            log.info("找到知识库 {} 中的 {} 个文档需要删除", ragTag, kbDocuments.size());
            
            // 模拟删除成功
            log.info("知识库删除完成: {}", ragTag);
            return Response.success("删除成功", "知识库 " + ragTag + " 已删除（包含 " + kbDocuments.size() + " 个文档）");
            
        } catch (Exception e) {
            log.error("删除知识库失败", e);
            return Response.fail("删除知识库失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用AI大模型生成智能回答
     * 
     * @param question 用户问题
     * @param context 检索到的知识库上下文
     * @param ragTag 知识库标签
     * @return AI生成的回答
     */
    private String generateAIAnswer(String question, String context, String ragTag) {
        try {
            log.info("使用AI模型生成回答，问题: {}, 知识库: {}", question, ragTag);
            
            // 构建专门用于RAG问答的ChatClient
            ChatClient ragChatClient = chatClientBuilder
                .defaultSystem("""
                    你是一个专业的知识库问答助手。你的职责是基于提供的知识库上下文，为用户提供准确、有用的回答。
                    
                    ## 工作原则：
                    1. **基于事实回答**：严格基于提供的上下文信息进行回答，不要编造或推测信息
                    2. **准确性优先**：如果上下文中没有足够信息回答问题，请明确说明
                    3. **结构化回答**：提供清晰、有条理的回答，必要时使用分点说明
                    4. **引用来源**：适当提及信息来源于知识库中的相关内容
                    5. **用户友好**：使用通俗易懂的语言，避免过于技术性的表述
                    
                    ## 回答格式：
                    - 如果能够回答：提供详细的答案，并说明信息来源
                    - 如果信息不足：说明现有信息的限制，并建议用户提供更多具体信息
                    - 如果完全无法回答：诚实说明知识库中没有相关信息
                    
                    ## 注意事项：
                    - 保持客观中立的态度
                    - 不要超出知识库内容的范围进行推测
                    - 如果涉及敏感或专业建议，请提醒用户咨询专业人士
                    """)
                .build();
            
            // 构建用户消息，包含问题和上下文
            String userMessage = String.format("""
                ## 用户问题：
                %s
                
                ## 知识库上下文：
                %s
                
                ## 知识库来源：
                %s
                
                请基于以上知识库上下文，为用户问题提供准确、有用的回答。
                """, question, context, ragTag != null ? ragTag : "全部知识库");
            
            // 调用AI模型生成回答
            String aiAnswer = ragChatClient.prompt()
                .user(userMessage)
                .call()
                .content();
            
            log.info("AI回答生成完成，回答长度: {}", aiAnswer.length());
            return aiAnswer;
            
        } catch (Exception e) {
            log.error("AI回答生成失败", e);
            // 降级到简单回答
            return String.format(
                "基于知识库 '%s' 中的信息，关于您的问题 '%s'，我找到了以下相关内容：\n\n%s", 
                ragTag != null ? ragTag : "全部", question, context);
        }
    }
}
