package pro.sky.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReminderService {

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private TelegramBotService telegramBotService;

    private static final Pattern REMINDER_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.+)");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public boolean parseAndSaveNotification(Long chatId, String text) {
        Matcher matcher = REMINDER_PATTERN.matcher(text);
        if (matcher.matches()) {
            String dateTimeStr = matcher.group(1);
            String messageText = matcher.group(2);

            try {
                LocalDateTime notificationDate = LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);

                if (notificationDate.isBefore(LocalDateTime.now())) {
                    return false;
                }

                NotificationTask notification = new NotificationTask(chatId, messageText, notificationDate);
                notificationTaskRepository.save(notification);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }
        return false;
    }

    public List<NotificationTask> getUserNotifications(Long chatId) {
        return notificationTaskRepository.findActiveNotificationsByChatId(chatId);
    }


    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<NotificationTask> dueNotifications = notificationTaskRepository.findDueNotifications(now);

        for (NotificationTask notification : dueNotifications) {
            String message = "⏰ Напоминание: " + notification.getMessageText();
            telegramBotService.sendMessage(notification.getChatId(), message);
            notification.setSent(true);
            notificationTaskRepository.save(notification);
        }
    }
}