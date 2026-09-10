package com.bayerna.reserveforces.adapter.web;

import com.bayerna.reserveforces.adapter.web.dto.UnitResponse;
import com.bayerna.reserveforces.repository.UnitRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** NFR-이식성: 소집부대·훈련장 정보를 데이터로 관리하여 관리자 화면의 부대 필터로 사용한다. */
@RestController
@RequestMapping("/api/admin/units")
public class AdminUnitController {

    private final UnitRepository unitRepository;

    public AdminUnitController(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @GetMapping
    public List<UnitResponse> listUnits() {
        return unitRepository.findAll().stream().map(UnitResponse::from).toList();
    }
}
