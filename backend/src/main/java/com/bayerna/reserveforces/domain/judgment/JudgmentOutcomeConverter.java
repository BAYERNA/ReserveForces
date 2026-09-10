package com.bayerna.reserveforces.domain.judgment;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JudgmentOutcomeConverter implements AttributeConverter<JudgmentOutcome, String> {

    @Override
    public String convertToDatabaseColumn(JudgmentOutcome attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public JudgmentOutcome convertToEntityAttribute(String dbData) {
        return dbData == null ? null : JudgmentOutcome.fromCode(dbData);
    }
}
