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

    @Query(value = """
            WITH classified AS (
                SELECT currency, CASE
                    WHEN currency = 'INR' THEN CASE WHEN base_salary < 4100000 THEN 'Below 4.1M' WHEN base_salary < 6150000 THEN '4.1M–6.15M' WHEN base_salary < 8200000 THEN '6.15M–8.2M' WHEN base_salary < 12300000 THEN '8.2M–12.3M' ELSE '12.3M+' END
                    WHEN currency = 'GBP' THEN CASE WHEN base_salary < 40000 THEN 'Below 40k' WHEN base_salary < 60000 THEN '40k–60k' WHEN base_salary < 80000 THEN '60k–80k' WHEN base_salary < 120000 THEN '80k–120k' ELSE '120k+' END
                    WHEN currency = 'EUR' THEN CASE WHEN base_salary < 46000 THEN 'Below 46k' WHEN base_salary < 69000 THEN '46k–69k' WHEN base_salary < 92000 THEN '69k–92k' WHEN base_salary < 138000 THEN '92k–138k' ELSE '138k+' END
                    WHEN currency = 'CAD' THEN CASE WHEN base_salary < 67500 THEN 'Below 67.5k' WHEN base_salary < 101250 THEN '67.5k–101.25k' WHEN base_salary < 135000 THEN '101.25k–135k' WHEN base_salary < 202500 THEN '135k–202.5k' ELSE '202.5k+' END
                    WHEN currency = 'AUD' THEN CASE WHEN base_salary < 74000 THEN 'Below 74k' WHEN base_salary < 111000 THEN '74k–111k' WHEN base_salary < 148000 THEN '111k–148k' WHEN base_salary < 222000 THEN '148k–222k' ELSE '222k+' END
                    ELSE CASE WHEN base_salary < 50000 THEN 'Below 50k' WHEN base_salary < 75000 THEN '50k–75k' WHEN base_salary < 100000 THEN '75k–100k' WHEN base_salary < 150000 THEN '100k–150k' ELSE '150k+' END
                END AS band
                FROM employees WHERE employment_status = 'ACTIVE'
            ) SELECT currency, band, count(*) AS employeeCount FROM classified GROUP BY currency, band ORDER BY currency, band
            """, nativeQuery = true)
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
