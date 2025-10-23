package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.+)");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    @Autowired
    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        logger.info("Telegram Bot Updates Listener initialized");
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update.updateId());

                if (update.message() != null && update.message().text() != null) {
                    String messageText = update.message().text();
                    Long chatId = update.message().chat().id();
                    String userName = update.message().chat().firstName();

                    logger.info("Received message from {} ({}): {}", userName, chatId, messageText);

                    if ("/start".equals(messageText)) {
                        sendWelcomeMessage(chatId, userName);
                    } else if ("/help".equals(messageText)) {
                        sendHelpMessage(chatId);
                    } else if ("/list".equals(messageText)) {
                        sendTaskList(chatId);
                    } else {
                        processNotificationMessage(chatId, messageText, userName);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Error processing updates", e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendWelcomeMessage(Long chatId, String userName) {
        String welcomeText = String.format("""
            👋 Привет, %s!
            
            Я бот-напоминалка! Я помогу тебе не забывать о важных делах.
            
            📝 Чтобы создать напоминание, отправь сообщение в формате:
            <b>дд.мм.гггг чч:мм Текст напоминания</b>
            
            Например:
            <code>25.12.2024 20:00 Поздравить друзей с Рождеством</code>
            <code>01.01.2025 00:00 С Новым Годом!</code>
            
            📋 Доступные команды:
            /start - начать работу
            /help - помощь
            /list - показать активные напоминания
            
            ⏰ Я пришлю напоминание в указанное время!
            """, userName);

        SendMessage message = new SendMessage(chatId, welcomeText);
        message.parseMode(ParseMode.valueOf("HTML"));
        executeMessage(message);
    }

    private void sendHelpMessage(Long chatId) {
        String helpText = """
            📖 <b>Помощь по использованию бота</b>
            
            <b>Создание напоминания:</b>
            Отправь сообщение в формате:
            <code>дд.мм.гггг чч:мм Текст напоминания</code>
            
            <b>Примеры:</b>
            <code>25.12.2024 20:00 Поздравить друзей</code>
            <code>01.01.2025 00:00 С Новым Годом!</code>
            <code>15.03.2024 09:00 Сходить к врачу</code>
            
            <b>Доступные команды:</b>
            /start - начать работу
            /help - показать эту справку
            /list - показать активные напоминания
            
            <b>Важно:</b>
            • Дата и время должны быть в будущем
            • Формат даты: дд.мм.гггг чч:мм
            • Время указывается в 24-часовом формате
            """;

        SendMessage message = new SendMessage(chatId, helpText);
        message.parseMode(ParseMode.valueOf("HTML"));
        executeMessage(message);
    }

    private void sendTaskList(Long chatId) {
        try {
            List<NotificationTask> tasks = notificationTaskRepository.findByChatIdAndSentFalse(chatId);

            if (tasks.isEmpty()) {
                sendMessage(chatId, "📭 У вас нет активных напоминаний.");
                return;
            }

            StringBuilder tasksList = new StringBuilder("📋 <b>Ваши активные напоминания:</b>\n\n");

            for (int i = 0; i < tasks.size(); i++) {
                NotificationTask task = tasks.get(i);
                String formattedDate = task.getNotificationDateTime().format(dateTimeFormatter);
                tasksList.append(String.format("%d. <b>%s</b>\n   📝 %s\n\n",
                        i + 1, formattedDate, task.getMessageText()));
            }

            tasksList.append(String.format("Всего: %d напоминаний", tasks.size()));

            SendMessage message = new SendMessage(chatId, tasksList.toString());
            message.parseMode(ParseMode.valueOf("HTML"));
            executeMessage(message);

        } catch (Exception e) {
            logger.error("Error getting task list for chat: {}", chatId, e);
            sendMessage(chatId, "❌ Произошла ошибка при получении списка напоминаний.");
        }
    }

    private void processNotificationMessage(Long chatId, String messageText, String userName) {
        Matcher matcher = pattern.matcher(messageText);

        if (matcher.matches()) {
            try {
                String dateTimeString = matcher.group(1);
                String notificationText = matcher.group(2).trim();

                if (notificationText.isEmpty()) {
                    sendMessage(chatId, "❌ Текст напоминания не может быть пустым.");
                    return;
                }

                LocalDateTime notificationDateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
                LocalDateTime now = LocalDateTime.now();

                if (notificationDateTime.isBefore(now)) {
                    sendMessage(chatId, "❌ Нельзя установить напоминание на прошедшее время.");
                    return;
                }

                if (notificationDateTime.isAfter(now.plusYears(1))) {
                    sendMessage(chatId, "❌ Нельзя установить напоминание более чем на год вперед.");
                    return;
                }

                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setChatId(chatId);
                notificationTask.setMessageText(notificationText);
                notificationTask.setNotificationDateTime(notificationDateTime);
                notificationTask.setSent(false);

                NotificationTask savedTask = notificationTaskRepository.save(notificationTask);

                String response = String.format("""
                    ✅ <b>Напоминание установлено!</b>
                    
                    📅 <b>Дата и время:</b> %s
                    📝 <b>Текст:</b> %s
                    
                    ⏰ Я пришлю вам это напоминание в указанное время!
                    """, dateTimeString, notificationText);

                SendMessage message = new SendMessage(chatId, response);
                message.parseMode(ParseMode.valueOf("HTML"));
                executeMessage(message);

                logger.info("Notification task created: id={}, chatId={}, dateTime={}",
                        savedTask.getId(), chatId, dateTimeString);

            } catch (DateTimeParseException e) {
                logger.error("Error parsing date from message: {}", messageText, e);
                sendMessage(chatId, "❌ Неверный формат даты или времени.\n\n" +
                        "Правильный формат: <b>дд.мм.гггг чч:мм</b>\n" +
                        "Пример: <code>25.12.2024 20:00</code>");
            } catch (Exception e) {
                logger.error("Error processing notification message: {}", messageText, e);
                sendMessage(chatId, "❌ Произошла ошибка при создании напоминания. Попробуйте еще раз.");
            }
        } else {
            sendMessage(chatId, """
                ❌ <b>Неверный формат сообщения!</b>
                
                📝 Для создания напоминания используйте формат:
                <b>дд.мм.гггг чч:мм Текст напоминания</b>
                
                <b>Примеры:</b>
                <code>25.12.2024 20:00 Поздравить друзей с Рождеством</code>
                <code>01.01.2025 00:00 С Новым Годом!</code>
                
                💡 Для помощи используйте команду /help
                """);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledNotifications() {
        try {
            LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            logger.info("🔍 Checking notifications for: {}", currentDateTime);

            List<NotificationTask> tasks = notificationTaskRepository
                    .findByNotificationDateTimeLessThanEqualAndSentFalse(currentDateTime);

            logger.info("📨 Found {} notifications to send", tasks.size());

            for (NotificationTask task : tasks) {
                try {
                    String notificationMessage = String.format("""
                        🔔 <b>Напоминание!</b>
                        
                        📝 %s
                        
                        💫 Хорошего дня!
                        """, task.getMessageText());

                    SendMessage message = new SendMessage(task.getChatId(), notificationMessage);
                    message.parseMode(ParseMode.valueOf("HTML"));

                    if (executeMessage(message)) {
                        task.setSent(true);
                        notificationTaskRepository.save(task);
                        logger.info("✅ Notification sent to chat: {}, taskId: {}", task.getChatId(), task.getId());
                    } else {
                        logger.error("❌ Failed to send notification to chat: {}", task.getChatId());
                    }

                    Thread.sleep(100);

                } catch (Exception e) {
                    logger.error("Error sending notification for task: {}", task.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error in scheduled notifications task", e);
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        message.parseMode(ParseMode.valueOf("HTML"));
        executeMessage(message);
    }

    private boolean executeMessage(SendMessage message) {
        try {
            SendResponse response = telegramBot.execute(message);
            if (!response.isOk()) {
                logger.error("Failed to send message: {}", response.description());
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error sending message to chat: {}", message.getParameters().get("chat_id"), e);
            return false;
        }
    }
}