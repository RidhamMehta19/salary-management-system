package com.incubyte.salarymanagement.salary;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryHistoryResponse(UUID id, String currency, BigDecimal baseSalary, BigDecimal bonus,
                                    BigDecimal totalCompensation, LocalDate effectiveDate,
                                    String changeReason, Instant recordedAt) {
    public static SalaryHistoryResponse from(SalaryHistory history) {
        return new SalaryHistoryResponse(history.getId(), history.getCurrency(), history.getBaseSalary(),
                history.getBonus(), history.getTotalCompensation(), history.getEffectiveDate(),
                history.getChangeReason(), history.getRecordedAt());
    }
}
