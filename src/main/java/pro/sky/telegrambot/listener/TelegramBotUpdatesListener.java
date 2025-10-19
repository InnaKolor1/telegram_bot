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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    pro.sky.telegrambot.listener.Scheduler scheduler;

    @Autowired
    private TaskRepository repository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message().text().equalsIgnoreCase("/start")) {
                SendMessage message = new SendMessage(update.message().chat().id(),
                        "Hello, " + update.message().from().firstName() + "!");
                SendResponse response = telegramBot.execute(message);
            } else {
                String task = update.message().text();
                logger.info(task);
                Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
                Matcher matcher = pattern.matcher(task);
                if (matcher.matches()) {
                    System.out.println(matcher.group(1));
                    System.out.println(matcher.group(3));

                    LocalDateTime date = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    String theTask = matcher.group(3);
                    System.out.println(date);
                    System.out.println(theTask);

                    NotificationTask t = new NotificationTask();
                    t.setChatId(update.message().chat().id());
                    t.setTime(date);
                    t.setMessage(theTask);
                    repository.save(t);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void checkTask() {
        scheduler.runTask()
                .forEach((n) -> {
                    SendMessage message = new SendMessage(n.getChatId(), n.getMessage());
                    SendResponse response = telegramBot.execute(message);
                });
    }

}