package com.incubyte.salarymanagement.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() { return dashboardService.summary(); }

    @GetMapping("/by-department")
    public List<CompensationBreakdown> byDepartment() { return dashboardService.byDepartment(); }

    @GetMapping("/by-country")
    public List<CompensationBreakdown> byCountry() { return dashboardService.byCountry(); }

    @GetMapping("/salary-distribution")
    public List<SalaryDistributionBucket> salaryDistribution() { return dashboardService.salaryDistribution(); }
}
