package com.bayerna.reserveforces.adapter.web.dto;

import java.time.LocalDateTime;

/** actualEntryDatetime이 null이면 미입영으로 처리된다. */
public record RecordEntryRequest(LocalDateTime actualEntryDatetime) {
}
