package com.onepiece.xmz.onepiece.trigger.demo;


import com.onepiece.xmz.demoapi.IOllamaApiService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ollamaApi/")
public class OllamaApiController implements IOllamaApiService {

    @Resource
    private OllamaChatModel ollamaChatModel;

    /**
     * http://localhost:8090/api/v1/ollamaApi/generate?model=deepseek-r1:1.5b&message=1+1
     */
    @RequestMapping(value = "generate", method = RequestMethod.GET)
    @Override
    public ChatResponse generate(@RequestParam("model") String model, @RequestParam("message") String message) {
        return ollamaChatModel.call(new Prompt(message,
                OllamaOptions.builder()
                        .model(model)
                        .temperature(0.5)
                        .build()
        ));
    }

    /**
     * http://localhost:8090/api/v1/ollamaApi/generate_stream?model=deepseek-r1:1.5b&message=hi
     */
    @RequestMapping(value = "generate_stream", method = RequestMethod.GET)
    @Override
    public Flux<ChatResponse> generateStream(String model, String message) {
        return ollamaChatModel.stream(new Prompt(message,
                OllamaOptions.builder()
                        .model(model)
                        .temperature(0.5)
                        .build()
        ));
    }
}
