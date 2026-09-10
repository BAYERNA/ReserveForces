package com.bayerna.reserveforces.domain.judgment;

import com.bayerna.reserveforces.domain.reservist.Reservist;
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

/** 판정결과 (JUDGMENT_RESULT). docs/db-design.md 참조. FR-04, FR-05, FR-06, FR-09 연계. */
@Entity
@Table(name = "judgment_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "judgment_id")
    private Long judgmentId;

    @ManyToOne
    @JoinColumn(name = "reservist_id", nullable = false)
    private Reservist reservist;

    @Column(name = "judgment_type", nullable = false, length = 20)
    private JudgmentType judgmentType;

    @Column(name = "is_auto", nullable = false)
    private boolean auto;

    @Column(name = "result", nullable = false, length = 10)
    private JudgmentOutcome result;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "judged_at", nullable = false)
    private LocalDateTime judgedAt;

    @jakarta.persistence.PrePersist
    void onCreate() {
        if (judgedAt == null) {
            judgedAt = LocalDateTime.now();
        }
    }

    /** FR-06: 관리자가 예외 사유를 기재하고 결과를 수동으로 보정한다. */
    public void applyManualCorrection(JudgmentOutcome newResult, String correctionReason) {
        this.result = newResult;
        this.reason = correctionReason;
        this.auto = false;
        this.judgedAt = LocalDateTime.now();
    }
}
