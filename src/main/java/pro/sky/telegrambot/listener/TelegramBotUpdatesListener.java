package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.MessagesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.TaskRepository;
import pro.sky.telegrambot.listener.Scheduler;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TaskRepository helperTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                if (update.message() != null && update.message().text() != null) {
                    String messageText = update.message().text();
                    Long chatId = update.message().chat().id();

                    if ("/start".equals(messageText)) {
                        sendWelcomeMessage(chatId);
                    } else {
                        processNotificationMessage(chatId, messageText);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка", e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = """
         Привет мой милый друг 👋
         Давай пообщаемся?
                """;

        SendMessage message = new SendMessage(chatId, welcomeText);
        executeMessage(message);
    }

    private void processNotificationMessage(Long chatId, String messageText) {
        Matcher matcher = pattern.matcher(messageText);

        if (matcher.matches()) {
            try {
                String dateTimeString = matcher.group(1);
                String notificationText = matcher.group(3);

                LocalDateTime notificationDateTime = LocalDateTime.parse(
                        dateTimeString, dateTimeFormatter
                );

                if (notificationDateTime.isBefore(LocalDateTime.now())) {
                    sendMessage(chatId, "Невозможно установить напоминание на прошедшую дату");
                    return;
                }

                NotificationTask task = new NotificationTask(
                        chatId, notificationText, notificationDateTime
                );

                helperTaskRepository.save(task);

                String response = String.format(
                        "Напоминание сработает %s:\n%s",
                        dateTimeString, notificationText
                );
                sendMessage(chatId, response);

            } catch (Exception e) {
                logger.error("Ошибка обработки даты из сообщения: {}", messageText, e);
                sendMessage(chatId, "Проверьте формат ввода");
            }
        } else {
            sendMessage(chatId, """
                    Неверный формат сообщения!
                    
                    Используйте: dd.MM.yyyy HH:mm Текст напоминания
                    Пример: 01.01.2025 20:00 Сдать домашнюю работу
                    """);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledNotifications() {
        try {
            LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            logger.info("Проверка уведомлений для: {}", currentDateTime);

            List<NotificationTask> tasks = helperTaskRepository
                    .findAllByNotificationDateTime(currentDateTime);

            for (NotificationTask task : tasks) {
                String notificationMessage = String.format(
                        "Напоминать:\n%s",
                        task.getMessageText()
                );

                SendMessage message = new SendMessage(task.getChatId(), notificationMessage);
                if (executeMessage(message)) {
                    logger.info("Уведомление отправлено в чат: {}", task.getChatId());
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка отправки запланированных уведомлений", e);
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        executeMessage(message);
    }

    private boolean executeMessage(SendMessage message) {
        try {
            SendResponse response = telegramBot.execute(message);
            return response.isOk();
        } catch (Exception e) {
            logger.error("Ошибка отправки сообщения", e);
            return false;
        }
    }
}