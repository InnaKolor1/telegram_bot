package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.entity.NotificationTask;

public interface TaskRepository extends JpaRepository<NotificationTask, Long> {
}