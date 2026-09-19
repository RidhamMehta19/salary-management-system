package com.incubyte.salarymanagement.seed;

import com.incubyte.salarymanagement.department.Department;
import com.incubyte.salarymanagement.department.DepartmentCatalog;
import com.incubyte.salarymanagement.department.DepartmentRepository;
import com.incubyte.salarymanagement.employee.Employee;
import com.incubyte.salarymanagement.employee.EmployeeRepository;
import com.incubyte.salarymanagement.employee.EmploymentStatus;
import com.incubyte.salarymanagement.salary.SalaryHistory;
import com.incubyte.salarymanagement.salary.SalaryHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@Profile("seed")
@Order(2)
class DemoDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final int EMPLOYEE_COUNT = 10_000;
    private static final String[] FIRST_NAMES = {"Aarav", "Aisha", "Amelia", "Arjun", "Ava", "Benjamin", "Charlotte", "Daniel", "Emma", "Ethan", "Fatima", "Harper", "Ishaan", "James", "Layla", "Liam", "Maya", "Noah", "Olivia", "Priya", "Riya", "Sophia", "Vihaan", "William", "Zara"};
    private static final String[] LAST_NAMES = {"Anderson", "Brown", "Chen", "Davis", "Garcia", "Gupta", "Harris", "Johnson", "Khan", "Lee", "Martin", "Mehta", "Miller", "Patel", "Robinson", "Shah", "Singh", "Smith", "Taylor", "Thomas", "Walker", "Wilson", "Wong", "Young", "Zhang"};
    private static final List<Office> OFFICES = List.of(
            new Office("United States", "New York", "USD"), new Office("United Kingdom", "London", "GBP"),
            new Office("India", "Bengaluru", "INR"), new Office("Germany", "Berlin", "EUR"),
            new Office("Canada", "Toronto", "CAD"), new Office("Australia", "Sydney", "AUD"));
    private static final Map<String, Integer> BASE_BY_DEPARTMENT = Map.of(
            "Engineering", 105_000, "Product", 95_000, "Sales", 85_000, "Marketing", 72_000,
            "Finance", 82_000, "People", 68_000, "Operations", 64_000, "Customer Success", 70_000);

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;

    DemoDataSeeder(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                   SalaryHistoryRepository salaryHistoryRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            log.info("Seed profile active; employee data already exists, skipping demo seed");
            return;
        }
        Map<String, Department> departments = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getName, department -> department));
        Random random = new Random(20260919L);
        for (int start = 0; start < EMPLOYEE_COUNT; start += 250) {
            List<Employee> employees = new ArrayList<>(250);
            for (int index = start; index < Math.min(start + 250, EMPLOYEE_COUNT); index++) {
                employees.add(createEmployee(index, random, departments));
            }
            List<Employee> savedEmployees = employeeRepository.saveAll(employees);
            List<SalaryHistory> history = savedEmployees.stream().map(employee -> new SalaryHistory(employee,
                    employee.getCurrency(), employee.getBaseSalary(), employee.getBonus(), employee.getHireDate(),
                    "Initial compensation")).toList();
            salaryHistoryRepository.saveAll(history);
        }
        log.info("Seeded {} deterministic employee records", EMPLOYEE_COUNT);
    }

    private Employee createEmployee(int index, Random random, Map<String, Department> departments) {
        String departmentName = DepartmentCatalog.NAMES.get(index % DepartmentCatalog.NAMES.size());
        Office office = OFFICES.get(index % OFFICES.size());
        int base = BASE_BY_DEPARTMENT.get(departmentName);
        BigDecimal baseSalary = BigDecimal.valueOf(base + random.nextInt(55_000) - 12_000L)
                .multiply(currencyMultiplier(office.currency())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal bonus = baseSalary.multiply(BigDecimal.valueOf(0.04 + (index % 9) * 0.01)).setScale(2, RoundingMode.HALF_UP);
        String firstName = FIRST_NAMES[index % FIRST_NAMES.length];
        String lastName = LAST_NAMES[(index / FIRST_NAMES.length) % LAST_NAMES.length];
        return new Employee(String.format("EMP-%05d", index + 1), firstName, lastName,
                "employee." + (index + 1) + "@example.org", departments.get(departmentName),
                titleFor(departmentName), office.country(), office.location(), statusFor(index),
                LocalDate.of(2014, 1, 1).plusDays(index % 4200), office.currency(), baseSalary, bonus);
    }

    private BigDecimal currencyMultiplier(String currency) {
        return switch (currency) {
            case "INR" -> BigDecimal.valueOf(82);
            case "GBP" -> BigDecimal.valueOf(0.79);
            case "EUR" -> BigDecimal.valueOf(0.92);
            case "CAD" -> BigDecimal.valueOf(1.35);
            case "AUD" -> BigDecimal.valueOf(1.48);
            default -> BigDecimal.ONE;
        };
    }

    private EmploymentStatus statusFor(int index) {
        if (index % 25 == 0) return EmploymentStatus.INACTIVE;
        if (index % 31 == 0) return EmploymentStatus.ON_LEAVE;
        return EmploymentStatus.ACTIVE;
    }

    private String titleFor(String department) {
        return switch (department) {
            case "Engineering" -> "Software Engineer";
            case "Product" -> "Product Manager";
            case "Sales" -> "Account Executive";
            case "Marketing" -> "Marketing Specialist";
            case "Finance" -> "Financial Analyst";
            case "People" -> "People Partner";
            case "Operations" -> "Operations Manager";
            default -> "Customer Success Manager";
        };
    }

    private record Office(String country, String location, String currency) {
    }
}
