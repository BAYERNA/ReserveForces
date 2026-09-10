package com.bayerna.reserveforces.domain.reservist;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReservistStatusConverter implements AttributeConverter<ReservistStatus, String> {

    @Override
    public String convertToDatabaseColumn(ReservistStatus attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public ReservistStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ReservistStatus.fromCode(dbData);
    }
}
