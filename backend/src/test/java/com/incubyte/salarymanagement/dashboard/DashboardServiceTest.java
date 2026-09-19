package com.incubyte.salarymanagement.dashboard;

import com.incubyte.salarymanagement.employee.EmployeeRepository;
import com.incubyte.salarymanagement.employee.EmploymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock private DashboardRepository dashboardRepository;
    @Mock private EmployeeRepository employeeRepository;

    @Test
    void summarizesActiveWorkforceAndCurrencyTotals() {
        DashboardRepository.CurrencySummaryProjection usd = new DashboardRepository.CurrencySummaryProjection() {
            public String getCurrency() { return "USD"; }
            public Long getEmployeeCount() { return 12L; }
            public BigDecimal getTotalCompensation() { return new BigDecimal("1200000"); }
            public BigDecimal getAverageBaseSalary() { return new BigDecimal("90000"); }
        };
        when(dashboardRepository.summarizeByCurrency()).thenReturn(List.of(usd));
        when(employeeRepository.countByStatus(EmploymentStatus.ACTIVE)).thenReturn(12L);
        when(employeeRepository.count()).thenReturn(15L);

        DashboardSummaryResponse result = new DashboardService(dashboardRepository, employeeRepository).summary();

        assertThat(result.activeEmployees()).isEqualTo(12);
        assertThat(result.totalEmployees()).isEqualTo(15);
        assertThat(result.compensationByCurrency()).containsExactly(
                new CurrencySummary("USD", 12L, new BigDecimal("1200000"), new BigDecimal("90000")));
    }
}
