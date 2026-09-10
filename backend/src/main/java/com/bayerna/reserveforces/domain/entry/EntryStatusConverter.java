package com.bayerna.reserveforces.domain.entry;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EntryStatusConverter implements AttributeConverter<EntryStatus, String> {

    @Override
    public String convertToDatabaseColumn(EntryStatus attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public EntryStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EntryStatus.fromCode(dbData);
    }
}
