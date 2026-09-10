package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.domain.entry.EntryRecord;
import com.bayerna.reserveforces.domain.entry.EntryStatus;
import com.bayerna.reserveforces.domain.reservist.Reservist;
import com.bayerna.reserveforces.domain.reservist.ReservistStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservistResponse(
        Long reservistId,
        String displayName,
        BigDecimal residenceDistanceKm,
        Long unitId,
        String unitName,
        ReservistStatus status,
        LocalDateTime actualEntryDatetime,
        EntryStatus entryStatus) {

    public static ReservistResponse from(Reservist reservist, EntryRecord entryRecord) {
        return new ReservistResponse(
                reservist.getReservistId(),
                reservist.getDisplayName(),
                reservist.getResidenceDistanceKm(),
                reservist.getUnit().getUnitId(),
                reservist.getUnit().getUnitName(),
                reservist.getStatus(),
                entryRecord != null ? entryRecord.getActualEntryDatetime() : null,
                entryRecord != null ? entryRecord.getEntryStatus() : null);
    }
}
