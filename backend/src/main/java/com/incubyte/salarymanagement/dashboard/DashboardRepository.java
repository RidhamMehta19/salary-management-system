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
              case when e.currency = 'INR' then
                   case when e.baseSalary < 4100000 then 'Below 4.1M'
                        when e.baseSalary < 6150000 then '4.1M–6.15M'
                        when e.baseSalary < 8200000 then '6.15M–8.2M'
                        when e.baseSalary < 12300000 then '8.2M–12.3M'
                        else '12.3M+' end
                   when e.currency = 'GBP' then
                   case when e.baseSalary < 40000 then 'Below 40k'
                        when e.baseSalary < 60000 then '40k–60k'
                        when e.baseSalary < 80000 then '60k–80k'
                        when e.baseSalary < 120000 then '80k–120k'
                        else '120k+' end
                   when e.currency = 'EUR' then
                   case when e.baseSalary < 46000 then 'Below 46k'
                        when e.baseSalary < 69000 then '46k–69k'
                        when e.baseSalary < 92000 then '69k–92k'
                        when e.baseSalary < 138000 then '92k–138k'
                        else '138k+' end
                   when e.currency = 'CAD' then
                   case when e.baseSalary < 67500 then 'Below 67.5k'
                        when e.baseSalary < 101250 then '67.5k–101.25k'
                        when e.baseSalary < 135000 then '101.25k–135k'
                        when e.baseSalary < 202500 then '135k–202.5k'
                        else '202.5k+' end
                   when e.currency = 'AUD' then
                   case when e.baseSalary < 74000 then 'Below 74k'
                        when e.baseSalary < 111000 then '74k–111k'
                        when e.baseSalary < 148000 then '111k–148k'
                        when e.baseSalary < 222000 then '148k–222k'
                        else '222k+' end
                   else case when e.baseSalary < 50000 then 'Below 50k'
                        when e.baseSalary < 75000 then '50k–75k'
                        when e.baseSalary < 100000 then '75k–100k'
                        when e.baseSalary < 150000 then '100k–150k'
                        else '150k+' end end as band,
              count(e) as employeeCount
            from Employee e where e.status = com.incubyte.salarymanagement.employee.EmploymentStatus.ACTIVE
            group by e.currency,
              case when e.currency = 'INR' then
                   case when e.baseSalary < 4100000 then 'Below 4.1M'
                        when e.baseSalary < 6150000 then '4.1M–6.15M'
                        when e.baseSalary < 8200000 then '6.15M–8.2M'
                        when e.baseSalary < 12300000 then '8.2M–12.3M'
                        else '12.3M+' end
                   when e.currency = 'GBP' then
                   case when e.baseSalary < 40000 then 'Below 40k'
                        when e.baseSalary < 60000 then '40k–60k'
                        when e.baseSalary < 80000 then '60k–80k'
                        when e.baseSalary < 120000 then '80k–120k'
                        else '120k+' end
                   when e.currency = 'EUR' then
                   case when e.baseSalary < 46000 then 'Below 46k'
                        when e.baseSalary < 69000 then '46k–69k'
                        when e.baseSalary < 92000 then '69k–92k'
                        when e.baseSalary < 138000 then '92k–138k'
                        else '138k+' end
                   when e.currency = 'CAD' then
                   case when e.baseSalary < 67500 then 'Below 67.5k'
                        when e.baseSalary < 101250 then '67.5k–101.25k'
                        when e.baseSalary < 135000 then '101.25k–135k'
                        when e.baseSalary < 202500 then '135k–202.5k'
                        else '202.5k+' end
                   when e.currency = 'AUD' then
                   case when e.baseSalary < 74000 then 'Below 74k'
                        when e.baseSalary < 111000 then '74k–111k'
                        when e.baseSalary < 148000 then '111k–148k'
                        when e.baseSalary < 222000 then '148k–222k'
                        else '222k+' end
                   else case when e.baseSalary < 50000 then 'Below 50k'
                        when e.baseSalary < 75000 then '50k–75k'
                        when e.baseSalary < 100000 then '75k–100k'
                        when e.baseSalary < 150000 then '100k–150k'
                        else '150k+' end end
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
