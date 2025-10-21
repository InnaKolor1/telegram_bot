package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TelegramBotService {

    @Value("${bot.token}")
    private String botToken;

    private TelegramBot bot;


    @PostConstruct
    public void init() {
        bot = new TelegramBot(botToken);
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        SendResponse response = bot.execute(request);

        if (!response.isOk()) {
            System.err.println("Ошибка отправки сообщения: " + response.description());
        }
    }
}