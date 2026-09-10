package com.bayerna.reserveforces.domain.notice;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NoticeStatusConverter implements AttributeConverter<NoticeStatus, String> {

    @Override
    public String convertToDatabaseColumn(NoticeStatus attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public NoticeStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : NoticeStatus.fromCode(dbData);
    }
}
