package com.example.MyFirstTgBot;

import io.modelcontextprotocol.client.McpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
public class LLMService {
    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpToolProvider;
    private final ChatMemory chatMemory;

    public LLMService(OllamaChatModel chatModel, ToolCallbackProvider mcpToolProvider, JdbcTemplate jdbcTemplate){
        this.mcpToolProvider = mcpToolProvider;

        ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new PostgresChatMemoryRepositoryDialect())
                .build();

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(mcpToolProvider)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                ).build();
    }
    public String question(String chat_id,String userMessage){
        String content = chatClient.prompt()
                .system("Responses should not contain any mention of errors encountered while using" +
                        "the tools, and the response should not exceed 4096 characters.")
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chat_id))
                .call()
                .content();

        if (content == null || content.isBlank()) {
            return "⚠️ Модель не вернула ответ";
        }

        return content;
    }
}
