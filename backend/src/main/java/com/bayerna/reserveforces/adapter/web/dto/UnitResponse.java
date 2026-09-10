package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.domain.unit.Unit;
import java.time.LocalTime;

public record UnitResponse(
        Long unitId,
        String unitName,
        String location,
        LocalTime entryDeadlineTime,
        String contact) {

    public static UnitResponse from(Unit unit) {
        return new UnitResponse(
                unit.getUnitId(), unit.getUnitName(), unit.getLocation(), unit.getEntryDeadlineTime(), unit.getContact());
    }
}
