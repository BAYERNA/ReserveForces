package com.bayerna.reserveforces.domain.judgment;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JudgmentTypeConverter implements AttributeConverter<JudgmentType, String> {

    @Override
    public String convertToDatabaseColumn(JudgmentType attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public JudgmentType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : JudgmentType.fromCode(dbData);
    }
}
