package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.domain.notice.CallupNotice;
import com.bayerna.reserveforces.domain.notice.NoticeStatus;
import java.time.LocalDateTime;

public record NoticeResponse(
        Long noticeId,
        Long unitId,
        String unitName,
        String location,
        String contact,
        LocalDateTime scheduledDatetime,
        NoticeStatus noticeStatus) {

    public static NoticeResponse from(CallupNotice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getUnit().getUnitId(),
                notice.getUnit().getUnitName(),
                notice.getUnit().getLocation(),
                notice.getUnit().getContact(),
                notice.getScheduledDatetime(),
                notice.getNoticeStatus());
    }
}
