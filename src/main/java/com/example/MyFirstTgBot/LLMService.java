package com.example.MyFirstTgBot;

import io.modelcontextprotocol.client.McpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
public class LLMService {
    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpToolProvider;

    public LLMService(OllamaChatModel chatModel, ToolCallbackProvider mcpToolProvider){
        Arrays.stream(mcpToolProvider.getToolCallbacks()).forEach(toolCallback -> {
            System.out.println("--------------------------------------");
            System.out.println(toolCallback.getToolDefinition());
            System.out.println("--------------------------------------");
        });
        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(mcpToolProvider)
                .build();
        this.mcpToolProvider = mcpToolProvider;
    }
    public String question(String userMessage){
        String content = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            return "⚠️ Модель не вернула ответ";
        }

        return content;
    }
}
