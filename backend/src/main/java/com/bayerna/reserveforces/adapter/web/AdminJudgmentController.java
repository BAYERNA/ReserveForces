package com.bayerna.reserveforces.adapter.web;

import com.bayerna.reserveforces.adapter.web.dto.CorrectJudgmentRequest;
import com.bayerna.reserveforces.adapter.web.dto.JudgmentResponse;
import com.bayerna.reserveforces.application.JudgmentService;
import com.bayerna.reserveforces.config.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-04, FR-05: 자동판정 결과 조회, FR-06: 수동 보정 */
@RestController
@RequestMapping("/api/admin/judgments")
public class AdminJudgmentController {

    private final JudgmentService judgmentService;

    public AdminJudgmentController(JudgmentService judgmentService) {
        this.judgmentService = judgmentService;
    }

    @GetMapping
    public List<JudgmentResponse> listJudgments() {
        return judgmentService.listAllJudgments().stream().map(JudgmentResponse::from).toList();
    }

    @PutMapping("/{judgmentId}/correct")
    public JudgmentResponse correctJudgment(
            @PathVariable Long judgmentId,
            @Valid @RequestBody CorrectJudgmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser admin) {
        var corrected = judgmentService.correctJudgment(judgmentId, request.result(), request.reason(), admin.userId());
        return JudgmentResponse.from(corrected);
    }
}
