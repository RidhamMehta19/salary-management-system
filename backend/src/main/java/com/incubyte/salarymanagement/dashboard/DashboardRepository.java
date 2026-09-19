package com.incubyte.salarymanagement.dashboard;

import com.incubyte.salarymanagement.employee.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardRepository extends Repository<Employee, java.util.UUID> {
    @Query("""
            select e.currency as currency, count(e) as employeeCount, sum(e.baseSalary + e.bonus) as totalCompensation,
                   avg(e.baseSalary) as averageBaseSalary
            from Employee e where e.status = com.incubyte.salarymanagement.employee.EmploymentStatus.ACTIVE
            group by e.currency order by e.currency
            """)
    List<CurrencySummaryProjection> summarizeByCurrency();

    @Query("""
            select e.department.name as label, e.currency as currency, count(e) as employeeCount,
                   sum(e.baseSalary + e.bonus) as totalCompensation, avg(e.baseSalary) as averageBaseSalary
            from Employee e where e.status = com.incubyte.salarymanagement.employee.EmploymentStatus.ACTIVE
            group by e.department.name, e.currency order by e.department.name, e.currency
            """)
    List<CompensationBreakdownProjection> byDepartment();

    @Query("""
            select e.country as label, e.currency as currency, count(e) as employeeCount,
                   sum(e.baseSalary + e.bonus) as totalCompensation, avg(e.baseSalary) as averageBaseSalary
            from Employee e where e.status = com.incubyte.salarymanagement.employee.EmploymentStatus.ACTIVE
            group by e.country, e.currency order by e.country, e.currency
            """)
    List<CompensationBreakdownProjection> byCountry();

    @Query("""
            select e.currency as currency,
              case when e.baseSalary < 50000 then 'Below 50k'
                   when e.baseSalary < 75000 then '50k–75k'
                   when e.baseSalary < 100000 then '75k–100k'
                   when e.baseSalary < 150000 then '100k–150k'
                   else '150k+' end as band,
              count(e) as employeeCount
            from Employee e where e.status = com.incubyte.salarymanagement.employee.EmploymentStatus.ACTIVE
            group by e.currency,
              case when e.baseSalary < 50000 then 'Below 50k'
                   when e.baseSalary < 75000 then '50k–75k'
                   when e.baseSalary < 100000 then '75k–100k'
                   when e.baseSalary < 150000 then '100k–150k'
                   else '150k+' end
            order by e.currency, band
            """)
    List<SalaryDistributionProjection> salaryDistribution();

    interface CurrencySummaryProjection {
        String getCurrency();
        Long getEmployeeCount();
        BigDecimal getTotalCompensation();
        BigDecimal getAverageBaseSalary();
    }

    interface CompensationBreakdownProjection {
        String getLabel();
        String getCurrency();
        Long getEmployeeCount();
        BigDecimal getTotalCompensation();
        BigDecimal getAverageBaseSalary();
    }

    interface SalaryDistributionProjection {
        String getCurrency();
        String getBand();
        Long getEmployeeCount();
    }
}
