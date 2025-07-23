//package com.onepiece.xmz.onepiece.trigger.http;
//
//import com.onepiece.xmz.api.IRAGService;
//import com.onepiece.xmz.api.response.Response;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RList;
//import org.redisson.api.RedissonClient;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.reader.tika.TikaDocumentReader;
//import org.springframework.ai.transformer.splitter.TokenTextSplitter;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@Slf4j
//@RestController()
//@CrossOrigin("*")
//@RequestMapping("/api/v1/rag/")
//public class OllamaRAGController implements IRAGService {
//
//    @Resource
//    private OllamaChatModel ollamaChatModel;
//    @Resource
//    private TokenTextSplitter tokenTextSplitter;
////    @Resource
////    private SimpleVectorStore simpleVectorStore;
//    @Resource
//    private PgVectorStore pgVectorStore;
//    @Resource
//    private RedissonClient redissonClient;
//
//
//    /**
//     * 查询当前所有已上传的知识库标签列表
//     * URL: GET /query_rag_tag_list
//     * 返回值：
//     *  - List<String>: 所有知识库的标签（ragTag），来源于 Redis
//     */
//    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.GET)
//    @Override
//    public Response<List<String>> queryRagTagList() {
//        // 1. 从 Redis 中获取存储知识库标签的列表（之前上传文件时会往这个列表添加）
//        RList<String> elements = redissonClient.getList("ragTag");
//
//        // 2. 返回统一封装的响应对象，包含知识库标签列表
//        return Response.<List<String>>builder()
//                .code("0000")           // 自定义业务成功码
//                .info("调用成功")        // 提示信息
//                .data(elements)         // 真实数据：知识库标签列表
//                .build();
//    }
//
//
//    /**
//     * 知识库文件上传接口
//     * URL: POST /file/upload
//     * Content-Type: multipart/form-data
//     * 参数：
//     *  - ragTag: 知识库标签（用于标识属于哪个知识库）
//     *  - files: 上传的文件列表
//     */
//    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
//    @Override
//    public Response<String> uploadFile(@RequestParam String ragTag,
//                                       @RequestParam("file") List<MultipartFile> files) {
//        log.info("上传知识库开始 {}", ragTag);
//
//        // 遍历上传的多个文件
//        for (MultipartFile file : files) {
//
//            // 1. 使用 Tika 读取文件内容，转换为 Spring AI 的 Document 列表
//            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
//            List<Document> documents = documentReader.get();
//
//            // 2. 将长文档进行分片（根据 tokenTextSplitter 设置的规则，如按 token 数量切分）
//            //todo : 这里是按Token 数量进行的文档切片，容易破坏语义的完整性，可以选择使用阿里云百练创建知识库进行智能切分
//            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
//
//            // 3. 给原始文档 & 分片文档加 metadata，用于标识该文档属于哪个知识库
//            documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
//            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
//
//            // 4. 将分片后的文档上传到向量数据库（PgVector），用于后续语义检索
//            pgVectorStore.accept(documentSplitterList);
//
//            // 5. 记录知识库标签到 Redis（Redisson List），避免重复添加
//            RList<String> elements = redissonClient.getList("ragTag");
//            if (!elements.contains(ragTag)) {
//                elements.add(ragTag);
//            }
//        }
//
//        log.info("上传知识库完成 {}", ragTag);
//
//        // 返回上传成功的响应
//        return Response.<String>builder()
//                .code("0000") // 自定义响应码
//                .info("调用成功")
//                .build();
//    }
//
//
//}
