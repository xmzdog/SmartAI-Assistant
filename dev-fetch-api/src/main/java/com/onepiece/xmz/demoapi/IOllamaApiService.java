package com.onepiece.xmz.demoapi;




import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;


public interface IOllamaApiService {
    ChatResponse generate(String model, String message);

    Flux<ChatResponse> generateStream(String model, String message);

}

