package com.bayerna.reserveforces.domain.processlog;

import com.bayerna.reserveforces.domain.judgment.JudgmentResult;
import com.bayerna.reserveforces.domain.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 처리이력 (PROCESS_LOG). docs/db-design.md 참조. FR-10 연계. */
@Entity
@Table(name = "process_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "judgment_id", nullable = false)
    private JudgmentResult judgment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @jakarta.persistence.PrePersist
    void onCreate() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }
}
