package com.onepiece.xmz.onepiece.trigger.http;

import com.onepiece.xmz.api.IRAGService;
import com.onepiece.xmz.api.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/rag/")
public class OllamaRAGController implements IRAGService {

    @Resource
    private OllamaChatModel ollamaChatModel;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private RedissonClient redissonClient;


    /**
     * 查询当前所有已上传的知识库标签列表
     * URL: GET /query_rag_tag_list
     * 返回值：
     *  - List<String>: 所有知识库的标签（ragTag），来源于 Redis
     */
    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.GET)
    @Override
    public Response<List<String>> queryRagTagList() {
        // 1. 从 Redis 中获取存储知识库标签的列表（之前上传文件时会往这个列表添加）
        RList<String> elements = redissonClient.getList("ragTag");

        // 2. 返回统一封装的响应对象，包含知识库标签列表
        return Response.<List<String>>builder()
                .code("0000")           // 自定义业务成功码
                .info("调用成功")        // 提示信息
                .data(elements)         // 真实数据：知识库标签列表
                .build();
    }


    /**
     * 知识库文件上传接口
     * URL: POST /file/upload
     * Content-Type: multipart/form-data
     * 参数：
     *  - ragTag: 知识库标签（用于标识属于哪个知识库）
     *  - files: 上传的文件列表
     */
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    @Override
    public Response<String> uploadFile(@RequestParam("ragTag") String ragTag,
                                       @RequestParam("file") List<MultipartFile> files) {
        log.info("上传知识库开始 {}", ragTag);

        // 遍历上传的多个文件
        for (MultipartFile file : files) {

            // 1. 使用 Tika 读取文件内容，转换为 Spring AI 的 Document 列表
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();

            // 2. 将长文档进行分片（根据 tokenTextSplitter 设置的规则，如按 token 数量切分）
            //todo : 这里是按Token 数量进行的文档切片，容易破坏语义的完整性，可以选择使用阿里云百练创建知识库进行智能切分
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // 3. 给原始文档 & 分片文档加 metadata，用于标识该文档属于哪个知识库
            documents.forEach(doc -> doc.getMetadata().put("knowledge_base", ragTag));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge_base", ragTag));

            // 4. 将分片后的文档上传到向量数据库（PgVector），用于后续语义检索
            pgVectorStore.accept(documentSplitterList);
//            simpleVectorStore.accept(documentSplitterList);

            // 5. 记录知识库标签到 Redis（Redisson List），避免重复添加
            RList<String> elements = redissonClient.getList("ragTag");
            if (!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }

        log.info("上传知识库完成 {}", ragTag);

        // 返回上传成功的响应
        return Response.<String>builder()
                .code("0000") // 自定义响应码
                .info("调用成功")
                .build();
    }

    /**
     * 基于知识库的问答接口
     * URL: POST /ask
     * Content-Type: application/json
     * 参数：
     *  - ragTag: 知识库标签
     *  - question: 用户问题
     */
    @RequestMapping(value = "ask", method = RequestMethod.POST)
    @Override
    public Response<String> askQuestion(@RequestBody Map<String, String> request) {
        String ragTag = request.get("ragTag");
        String question = request.get("question");
        
        log.info("知识库问答开始，知识库: {}, 问题: {}", ragTag, question);

        try {
            // 1. 基于用户问题，从向量数据库中检索相关文档片段
            List<Document> similarDocuments = pgVectorStore.similaritySearch(
                SearchRequest.builder().query(question)
                    .topK(5) // 返回最相似的5个文档片段
                    .similarityThreshold(0.7) // 相似度阈值
                    .filterExpression("knowledge_base == '" + ragTag + "'")
                        .build()// 过滤特定知识库
            );

            // 2. 如果没有找到相关文档，返回提示信息
            if (similarDocuments.isEmpty()) {
                return Response.<String>builder()
                        .code("0001")
                        .info("在指定知识库中未找到相关信息")
                        .data("抱歉，我在知识库「" + ragTag + "」中没有找到与您问题相关的信息。请尝试重新表述问题或上传相关文档。")
                        .build();
            }

            // 3. 构建上下文内容（将检索到的文档片段合并）
            StringBuilder contextBuilder = new StringBuilder();
            for (Document doc : similarDocuments) {
                contextBuilder.append(doc.getText()).append("\n\n");
            }
            String context = contextBuilder.toString();

            // 4. 构建提示词模板
            String promptTemplate = """
                请基于以下上下文信息回答用户的问题。如果上下文信息不足以回答问题，请明确说明。
                
                上下文信息：
                {context}
                
                用户问题：{question}
                
                请提供准确、有用的回答：
                """;

            // 5. 创建提示词
            PromptTemplate template = new PromptTemplate(promptTemplate);
            Prompt prompt = template.create(Map.of(
                "context", context,
                "question", question
            ));

            // 6. 调用大语言模型生成回答
            ChatResponse response = ollamaChatModel.call(prompt);

            String answer = response.getResult().getOutput().getText();

            log.info("知识库问答完成，知识库: {}", ragTag);

            // 7. 返回回答结果
            return Response.<String>builder()
                    .code("0000")
                    .info("调用成功")
                    .data(answer)
                    .build();

        } catch (Exception e) {
            log.error("知识库问答失败，知识库: {}, 问题: {}, 错误: {}", ragTag, question, e.getMessage(), e);
            return Response.<String>builder()
                    .code("0002")
                    .info("问答处理失败")
                    .data("抱歉，处理您的问题时出现了错误，请稍后重试。")
                    .build();
        }
    }

    /**
     * 删除知识库接口
     * URL: DELETE /knowledge-base/{ragTag}
     */
    @RequestMapping(value = "knowledge-base/{ragTag}", method = RequestMethod.DELETE)
    @Override
    public Response<String> deleteKnowledgeBase(@PathVariable("ragTag") String ragTag) {
        log.info("删除知识库开始: {}", ragTag);
        
        try {
            // 1. 从Redis中移除知识库标签
            RList<String> elements = redissonClient.getList("ragTag");
            elements.remove(ragTag);
            
            // 2. 从向量数据库中删除相关文档
            // 注意：PgVectorStore没有直接的删除方法，这里需要自定义实现
            // 或者标记为已删除状态
            
            log.info("删除知识库完成: {}", ragTag);
            
            return Response.<String>builder()
                    .code("0000")
                    .info("删除成功")
                    .build();
        } catch (Exception e) {
            log.error("删除知识库失败: {}, 错误: {}", ragTag, e.getMessage(), e);
            return Response.<String>builder()
                    .code("0003")
                    .info("删除失败")
                    .data("删除知识库时出现错误，请稍后重试。")
                    .build();
        }
    }


}
