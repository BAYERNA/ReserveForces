package com.bayerna.reserveforces.domain.judgment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JudgmentRuleEngineTest {

    private final JudgmentRuleEngine engine = new JudgmentRuleEngine();

    @Test
    void 정시입영은_지연입소_판정이_생성되지_않는다() {
        LocalDateTime deadline = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime actual = LocalDateTime.of(2026, 9, 10, 8, 55);

        JudgmentRuleEngine.LateEntryJudgment result = engine.judgeLateEntry(deadline, actual);

        assertThat(result.applicable()).isFalse();
        assertThat(result.entryStatus()).isEqualTo(com.bayerna.reserveforces.domain.entry.EntryStatus.COMPLETED);
    }

    @Test
    void 한시간_이내_지연은_허용된다() {
        LocalDateTime deadline = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime actual = LocalDateTime.of(2026, 9, 10, 9, 59);

        JudgmentRuleEngine.LateEntryJudgment result = engine.judgeLateEntry(deadline, actual);

        assertThat(result.applicable()).isTrue();
        assertThat(result.outcome()).isEqualTo(JudgmentOutcome.ALLOWED);
    }

    @Test
    void 한시간_초과_지연은_불허된다() {
        LocalDateTime deadline = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime actual = LocalDateTime.of(2026, 9, 10, 10, 1);

        JudgmentRuleEngine.LateEntryJudgment result = engine.judgeLateEntry(deadline, actual);

        assertThat(result.applicable()).isTrue();
        assertThat(result.outcome()).isEqualTo(JudgmentOutcome.DENIED);
    }

    @Test
    void 미입영은_판정없이_미입영_상태로_처리된다() {
        LocalDateTime deadline = LocalDateTime.of(2026, 9, 10, 9, 0);

        JudgmentRuleEngine.LateEntryJudgment result = engine.judgeLateEntry(deadline, null);

        assertThat(result.applicable()).isFalse();
        assertThat(result.entryStatus()).isEqualTo(com.bayerna.reserveforces.domain.entry.EntryStatus.NO_SHOW);
    }

    @Test
    void 거리가_100km_이상이면_조기퇴소가_허용된다() {
        JudgmentRuleEngine.EarlyDischargeJudgment result = engine.judgeEarlyDischarge(BigDecimal.valueOf(100.0));
        assertThat(result.outcome()).isEqualTo(JudgmentOutcome.ALLOWED);
    }

    @Test
    void 거리가_100km_미만이면_조기퇴소가_불허된다() {
        JudgmentRuleEngine.EarlyDischargeJudgment result = engine.judgeEarlyDischarge(BigDecimal.valueOf(99.9));
        assertThat(result.outcome()).isEqualTo(JudgmentOutcome.DENIED);
    }
}
