package com.chatbot.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatModelConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatModelConfig.class);

    @Bean
    @Primary
    public ChatModel chatModel(OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }

    @Bean
    public RestClientCustomizer loggingRestClientCustomizer() {
        return restClientBuilder -> restClientBuilder.requestInterceptor((request, body, execution) -> {
            log.info("OpenAI request URI: {} {}", request.getMethod(), request.getURI());
            return execution.execute(request, body);
        });
    }
}
