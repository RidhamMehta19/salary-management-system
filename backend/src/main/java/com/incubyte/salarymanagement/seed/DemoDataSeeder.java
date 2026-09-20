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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Profile("seed")
@Order(2)
class DemoDataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final int EMPLOYEE_COUNT = 10_000;
    private static final int CHUNK_SIZE = 500;
    private static final Instant SEEDED_AT = Instant.parse("2026-01-01T00:00:00Z");
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
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService seedExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "demo-data-seeder");
        thread.setDaemon(true);
        return thread;
    });

    DemoDataSeeder(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                   SalaryHistoryRepository salaryHistoryRepository, PlatformTransactionManager transactionManager) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @EventListener(ApplicationReadyEvent.class)
    void seedInBackground() {
        seedExecutor.submit(this::seed);
    }

    private void seed() {
            log.info("Starting deterministic demo data seed");
        try {
            long existingCount = employeeRepository.count();
            boolean isPartialSeed = existingCount > 0
                    && employeeRepository.existsById(deterministicId("employee-0"));
            if (existingCount >= EMPLOYEE_COUNT || (existingCount > 0 && !isPartialSeed)) {
                log.info("Seed profile active; employee data already exists, skipping demo seed");
                return;
            }
            Map<String, Department> departments = departmentRepository.findAll().stream()
                    .collect(Collectors.toMap(Department::getName, department -> department));
            Random random = new Random(20260919L);
            int start = (int) existingCount;
            for (int index = 0; index < start; index++) {
                createEmployee(index, random, departments);
            }
            for (; start < EMPLOYEE_COUNT; start += CHUNK_SIZE) {
                int chunkStart = start;
                int chunkEnd = Math.min(start + CHUNK_SIZE, EMPLOYEE_COUNT);
                transactionTemplate.executeWithoutResult(status -> seedChunk(chunkStart, chunkEnd, random, departments));
                log.info("Seeded {}/{} employees", chunkEnd, EMPLOYEE_COUNT);
            }
        } catch (Exception exception) {
            log.error("Demo data seed failed", exception);
        } finally {
            log.info("Finished deterministic demo data seed");
        }
    }

    private void seedChunk(int start, int end, Random random, Map<String, Department> departments) {
        List<Employee> employees = new ArrayList<>(end - start);
        for (int index = start; index < end; index++) {
            employees.add(createEmployee(index, random, departments));
        }
        List<Employee> savedEmployees = employeeRepository.saveAll(employees);
        List<SalaryHistory> history = savedEmployees.stream().map(employee -> new SalaryHistory(
                deterministicId("salary-history-" + employee.getEmployeeNumber()), employee,
                employee.getCurrency(), employee.getBaseSalary(), employee.getBonus(), employee.getHireDate(),
                "Initial compensation", SEEDED_AT)).toList();
        salaryHistoryRepository.saveAll(history);
    }

    @PreDestroy
    void shutdown() {
        seedExecutor.shutdown();
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
        return new Employee(deterministicId("employee-" + index), String.format("EMP-%05d", index + 1), firstName, lastName,
                "employee." + (index + 1) + "@example.org", departments.get(departmentName),
                titleFor(departmentName), office.country(), office.location(), statusFor(index),
                LocalDate.of(2014, 1, 1).plusDays(index % 4200), office.currency(), baseSalary, bonus, SEEDED_AT, SEEDED_AT);
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

    private UUID deterministicId(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private record Office(String country, String location, String currency) {
    }
}
