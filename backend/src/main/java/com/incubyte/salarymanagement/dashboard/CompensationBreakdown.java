package com.incubyte.salarymanagement.dashboard;

import java.math.BigDecimal;

public record CompensationBreakdown(String label, String currency, long employeeCount,
                                    BigDecimal totalCompensation, BigDecimal averageBaseSalary) {
}
