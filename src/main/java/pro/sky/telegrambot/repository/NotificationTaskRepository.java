package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByChatId(Long chatId);

    @Query("SELECT n FROM NotificationTask n WHERE n.notificationDate <= :currentTime AND n.sent = false")
    List<NotificationTask> findDueNotifications(@Param("currentTime") LocalDateTime currentTime);


    @Query("SELECT n FROM NotificationTask n WHERE n.chatId = :chatId AND n.sent = false ORDER BY n.notificationDate ASC")
    List<NotificationTask> findActiveNotificationsByChatId(@Param("chatId") Long chatId);
}