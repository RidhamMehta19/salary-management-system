package com.incubyte.salarymanagement.dashboard;

import java.util.List;

public record DashboardSummaryResponse(long activeEmployees, long totalEmployees, List<CurrencySummary> compensationByCurrency) {
}
