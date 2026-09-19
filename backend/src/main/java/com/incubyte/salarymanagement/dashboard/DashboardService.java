package com.incubyte.salarymanagement.dashboard;

import com.incubyte.salarymanagement.employee.EmployeeRepository;
import com.incubyte.salarymanagement.employee.EmploymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final EmployeeRepository employeeRepository;

    public DashboardService(DashboardRepository dashboardRepository, EmployeeRepository employeeRepository) {
        this.dashboardRepository = dashboardRepository;
        this.employeeRepository = employeeRepository;
    }

    public DashboardSummaryResponse summary() {
        List<CurrencySummary> totals = dashboardRepository.summarizeByCurrency().stream()
                .map(item -> new CurrencySummary(item.getCurrency(), item.getEmployeeCount(),
                        item.getTotalCompensation(), item.getAverageBaseSalary())).toList();
        return new DashboardSummaryResponse(employeeRepository.countByStatus(EmploymentStatus.ACTIVE),
                employeeRepository.count(), totals);
    }

    public List<CompensationBreakdown> byDepartment() {
        return dashboardRepository.byDepartment().stream().map(item -> new CompensationBreakdown(item.getLabel(),
                item.getCurrency(), item.getEmployeeCount(), item.getTotalCompensation(), item.getAverageBaseSalary())).toList();
    }

    public List<CompensationBreakdown> byCountry() {
        return dashboardRepository.byCountry().stream().map(item -> new CompensationBreakdown(item.getLabel(),
                item.getCurrency(), item.getEmployeeCount(), item.getTotalCompensation(), item.getAverageBaseSalary())).toList();
    }

    public List<SalaryDistributionBucket> salaryDistribution() {
        return dashboardRepository.salaryDistribution().stream().map(item -> new SalaryDistributionBucket(
                item.getCurrency(), item.getBand(), item.getEmployeeCount())).toList();
    }
}
