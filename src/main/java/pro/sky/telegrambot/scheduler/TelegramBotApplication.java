package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableScheduling
public class TelegramBotApplication {

	public TelegramBotApplication(Class<DefaultBotSession> defaultBotSessionClass) {
	}

	public static void main(String[] args) {
		SpringApplication.run(TelegramBotApplication.class, args);
	}

	public void registerBot(TelegramBot bot) {
	}
}
