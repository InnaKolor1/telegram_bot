package pro.sky.telegrambot.model;
import pro.sky.telegrambot.model.Reminder;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "reminder_text", length = 1000, nullable = false)
    private String reminderText;

    @Column(name = "reminder_date", nullable = false)
    private LocalDateTime reminderDate;

    @Column(name = "is_sent")
    private Boolean isSent = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Reminder() {}

    public Reminder(Long chatId, String reminderText, LocalDateTime reminderDate) {
        this.chatId = chatId;
        this.reminderText = reminderText;
        this.reminderDate = reminderDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getReminderText() { return reminderText; }
    public void setReminderText(String reminderText) { this.reminderText = reminderText; }

    public LocalDateTime getReminderDate() { return reminderDate; }
    public void setReminderDate(LocalDateTime reminderDate) { this.reminderDate = reminderDate; }


    public Boolean getIsSent() { return isSent; }
    public void setIsSent(Boolean isSent) { this.isSent = isSent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
