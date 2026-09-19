package com.incubyte.salarymanagement.dashboard;

import java.math.BigDecimal;

public record CurrencySummary(String currency, long employeeCount, BigDecimal totalCompensation, BigDecimal averageBaseSalary) {
}
