package com.chatbot.demo.controller;

import com.chatbot.demo.utils.ResponseFormatter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.ResponseEntity;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;

    ChatController(ChatClient.Builder builder,
                   ToolCallbackProvider tools, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor()
                )
                .defaultToolCallbacks(tools)
                .build();
    }

    @PostMapping("/ask")
    public Output ask(@RequestBody Question prompt, @CookieValue(name = "X-CONV-ID", required = false) String convId) {
        var response = chatClient.prompt()
                .user(prompt.prompt())
                .call()
                .content();
        log.debug("Response from AI: {}", response);
        String formattedAnswer = formatResponse(prompt, response);
        log.debug("Formatted response: {}", formattedAnswer);
        var htmlResponse = ResponseFormatter.toHtml(formattedAnswer);
        log.debug("HTML formatted response: {}", htmlResponse);
        return new Output(htmlResponse);
    }

// For production: This uses persistent chat for each user, may need to have a data retention policy for the chat
// I may still pretty much stick with Inmemory chat persistence
//    @PostMapping("/ask")
//    ResponseEntity<Output> chat(@RequestBody Question prompt,
//                                @CookieValue(name = "X-CONV-ID", required = false) String convId) {
//        String conversationId = convId == null ? UUID.randomUUID().toString() : convId;
//        var response = this.chatClient.prompt()
//                .user(prompt.prompt())
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call().content();
//        String markdownAnswer = formatResponse(prompt, response);
//        var htmlResponse = MarkdownHelper.toHTML(markdownAnswer);
//
//        ResponseCookie cookie = ResponseCookie.from("X-CONV-ID", conversationId)
//                .path("/")
//                .maxAge(3600)
//                .build();
//        Output output = new Output(htmlResponse);
//        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(output);
//    }

    private String formatResponse(Question prompt, String answer) {
        var systemPrompt = """
                You are formatting an AI assistant response.
                
                Question: {question}
                
                Answer: {answer}
                
                Return a concise plain text answer. Use the first line with pipe (`|`) separators for table headers if structured data is required; subsequent lines, also pipe-separated, become table rows. For single values that do not require a table, return plain sentences without pipes.
                """;
        return chatClient
                .prompt()
                .user(u -> u.text(systemPrompt)
                        .param("question", prompt.prompt())
                        .param("answer", answer)
                )
                .call()
                .content();
    }

    public record Question(String prompt) {
    }

    record Output(String content) {
    }

}
