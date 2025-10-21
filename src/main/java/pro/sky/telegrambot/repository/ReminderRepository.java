package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.Reminder;

import java.time.LocalDateTime;
import java.util.List;
import pro.sky.telegrambot.repository.ReminderRepository;
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByChatId(Long chatId);

    @Query("SELECT r FROM Reminder r WHERE r.reminderDate <= :currentTime AND r.isSent = false")
    List<Reminder> findDueReminders(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT r FROM Reminder r WHERE r.chatId = :chatId AND r.isSent = false ORDER BY r.reminderDate ASC")
    List<Reminder> findActiveRemindersByChatId(@Param("chatId") Long chatId);
}