package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {


    @Query("SELECT n FROM NotificationTask n WHERE n.notificationDateTime <= :currentDateTime AND n.sent = false")
    List<NotificationTask> findDueNotifications(@Param("currentDateTime") LocalDateTime currentDateTime);


    List<NotificationTask> findByNotificationDateTimeLessThanEqualAndSentFalse(LocalDateTime dateTime);


    List<NotificationTask> findByChatId(Long chatId);

    List<NotificationTask> findByChatIdAndSentFalse(Long chatId);
}