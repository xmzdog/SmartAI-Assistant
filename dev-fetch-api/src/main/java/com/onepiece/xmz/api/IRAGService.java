package com.onepiece.xmz.api;

import com.onepiece.xmz.api.response.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IRAGService {

    Response<List<String>> queryRagTagList();

    Response<String> uploadFile(String ragTag, List<MultipartFile> files);

    Response<String> askQuestion(Map<String, String> request);

    Response<String> deleteKnowledgeBase(String ragTag);

}
