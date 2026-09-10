package com.bayerna.reserveforces.adapter.web;

import com.bayerna.reserveforces.adapter.web.dto.JudgmentResponse;
import com.bayerna.reserveforces.adapter.web.dto.NoticeResponse;
import com.bayerna.reserveforces.adapter.web.dto.UnitResponse;
import com.bayerna.reserveforces.application.JudgmentService;
import com.bayerna.reserveforces.application.NoticeService;
import com.bayerna.reserveforces.common.ApiException;
import com.bayerna.reserveforces.config.AuthenticatedUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예비군 본인용 API. FR-07(소집통지 확인), FR-08(부대·훈련장 정보 조회), FR-09(본인 판정 결과 확인).
 * 항상 JWT 토큰의 reservistId만을 조회 조건으로 사용하여 타인의 정보에 접근할 수 없도록 한다 (NFR-보안).
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final NoticeService noticeService;
    private final JudgmentService judgmentService;

    public MeController(NoticeService noticeService, JudgmentService judgmentService) {
        this.noticeService = noticeService;
        this.judgmentService = judgmentService;
    }

    @GetMapping("/notices")
    public List<NoticeResponse> myNotices(@AuthenticationPrincipal AuthenticatedUser user) {
        return noticeService.listNoticesForReservist(requireReservistId(user)).stream()
                .map(NoticeResponse::from)
                .toList();
    }

    @GetMapping("/unit")
    public UnitResponse myUnit(@AuthenticationPrincipal AuthenticatedUser user) {
        return UnitResponse.from(noticeService.getUnitForReservist(requireReservistId(user)));
    }

    @GetMapping("/judgments")
    public List<JudgmentResponse> myJudgments(@AuthenticationPrincipal AuthenticatedUser user) {
        return judgmentService.listJudgmentsForReservist(requireReservistId(user)).stream()
                .map(JudgmentResponse::from)
                .toList();
    }

    private Long requireReservistId(AuthenticatedUser user) {
        if (user.reservistId() == null) {
            throw ApiException.forbidden("예비군 계정에만 허용된 기능입니다.");
        }
        return user.reservistId();
    }
}
