package com.bayerna.reserveforces.adapter.web;

import com.bayerna.reserveforces.adapter.web.dto.ProcessLogResponse;
import com.bayerna.reserveforces.application.JudgmentService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-10: 판정·보정 처리 내역 조회 */
@RestController
@RequestMapping("/api/admin/process-logs")
public class AdminProcessLogController {

    private final JudgmentService judgmentService;

    public AdminProcessLogController(JudgmentService judgmentService) {
        this.judgmentService = judgmentService;
    }

    @GetMapping
    public List<ProcessLogResponse> listProcessLogs() {
        return judgmentService.listProcessLogs().stream().map(ProcessLogResponse::from).toList();
    }
}
