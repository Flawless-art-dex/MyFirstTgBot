package com.example.MyFirstTgBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class MyBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer{
    private final String botToken;
    private final TelegramClient telegramClient;
    private final SecurityService securityService;
    private final LLMService llmService;

    public MyBot(@Value("${bot.token}") String botToken, SecurityService securityService,LLMService llmService) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.securityService = securityService;
        this.llmService = llmService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            long user_id = update.getMessage().getFrom().getId();
            System.out.println("RAW MESSAGE: " + message_text);
            if(!securityService.isApproved(user_id)){
                SendMessage errMessage = SendMessage.builder()
                        .chatId(chat_id)
                        .text("⛔ У вас нет доступа")
                        .build();
                try {
                    telegramClient.execute(errMessage); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
            else {
                String answer;
                try{
                    answer = llmService.question(message_text);}
                catch (Exception e){
                    answer = "⚠️ Ошибка при обработке запроса";
                }
                SendMessage message = SendMessage // Create a message object
                        .builder()
                        .chatId(chat_id)
                        .text(answer)
                        .build();
                try {
                    telegramClient.execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

