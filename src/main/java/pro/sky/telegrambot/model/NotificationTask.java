package pro.sky.telegrambot.model;


import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "notification_task")
public class NotificationTask {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "notification_date", nullable = false)
    private LocalDateTime notificationDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent")
    private Boolean sent = false;

    public NotificationTask() {
    }

    public NotificationTask(Long chatId, String messageText, LocalDateTime notificationDate) {
        this.chatId = chatId;
        this.messageText = messageText;
        this.notificationDate = notificationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(LocalDateTime notificationDate) {
        this.notificationDate = notificationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", messageText='" + messageText + '\'' +
                ", notificationDate=" + notificationDate +
                ", createdAt=" + createdAt +
                ", sent=" + sent +
                '}';
    }
}