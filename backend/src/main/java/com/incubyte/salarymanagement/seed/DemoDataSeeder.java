package com.incubyte.salarymanagement.seed;

import com.incubyte.salarymanagement.department.Department;
import com.incubyte.salarymanagement.department.DepartmentCatalog;
import com.incubyte.salarymanagement.department.DepartmentRepository;
import com.incubyte.salarymanagement.employee.Employee;
import com.incubyte.salarymanagement.employee.EmploymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@Profile("seed")
@Order(2)
class DemoDataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final int EMPLOYEE_COUNT = 10_000;
    private static final int CHUNK_SIZE = 1_000;
    private static final int MAX_ATTEMPTS = 5;
    private static final Instant SEEDED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final String EMPLOYEE_INSERT = """
            INSERT INTO employees (id, employee_number, first_name, last_name, email, department_id,
                job_title, country, location, employment_status, hire_date, currency, base_salary, bonus,
                created_at, updated_at, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;
    private static final String SALARY_HISTORY_INSERT = """
            INSERT INTO salary_history (id, employee_id, currency, base_salary, bonus, effective_date,
                change_reason, recorded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;
    private static final String[] FIRST_NAMES = {"Aarav", "Aisha", "Amelia", "Arjun", "Ava", "Benjamin", "Charlotte", "Daniel", "Emma", "Ethan", "Fatima", "Harper", "Ishaan", "James", "Layla", "Liam", "Maya", "Noah", "Olivia", "Priya", "Riya", "Sophia", "Vihaan", "William", "Zara"};
    private static final String[] LAST_NAMES = {"Anderson", "Brown", "Chen", "Davis", "Garcia", "Gupta", "Harris", "Johnson", "Khan", "Lee", "Martin", "Mehta", "Miller", "Patel", "Robinson", "Shah", "Singh", "Smith", "Taylor", "Thomas", "Walker", "Wilson", "Wong", "Young", "Zhang"};
    private static final List<Office> OFFICES = List.of(
            new Office("United States", "New York", "USD"), new Office("United Kingdom", "London", "GBP"),
            new Office("India", "Bengaluru", "INR"), new Office("Germany", "Berlin", "EUR"),
            new Office("Canada", "Toronto", "CAD"), new Office("Australia", "Sydney", "AUD"));
    private static final Map<String, Integer> BASE_BY_DEPARTMENT = Map.of(
            "Engineering", 105_000, "Product", 95_000, "Sales", 85_000, "Marketing", 72_000,
            "Finance", 82_000, "People", 68_000, "Operations", 64_000, "Customer Success", 70_000);

    private final JdbcTemplate jdbcTemplate;
    private final DepartmentRepository departmentRepository;
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService seedExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "demo-data-seeder");
        thread.setDaemon(false);
        return thread;
    });
    private volatile boolean stopping;

    @Autowired
    DemoDataSeeder(JdbcTemplate jdbcTemplate, DepartmentRepository departmentRepository,
                   PlatformTransactionManager transactionManager) {
        this(jdbcTemplate, departmentRepository, new TransactionTemplate(transactionManager));
    }

    DemoDataSeeder(JdbcTemplate jdbcTemplate, DepartmentRepository departmentRepository,
                   TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.departmentRepository = departmentRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    void seedInBackground() {
        seedExecutor.submit(this::seed);
    }

    void seed() {
        log.info("Starting deterministic demo data seed");
        try {
            long employeeCount = count("employees");
            long salaryHistoryCount = count("salary_history");
            if (employeeCount >= EMPLOYEE_COUNT && salaryHistoryCount >= EMPLOYEE_COUNT) {
                log.info("Seed profile active; employee and salary-history data already exists, skipping demo seed");
                return;
            }

            Map<String, Department> departments = departmentRepository.findAll().stream()
                    .collect(Collectors.toMap(Department::getName, department -> department));
            Random random = new Random(20260919L);
            for (int start = 0; start < EMPLOYEE_COUNT; start += CHUNK_SIZE) {
                if (stopping || Thread.currentThread().isInterrupted()) {
                    log.info("Demo data seed stopped between chunks");
                    return;
                }
                int end = Math.min(start + CHUNK_SIZE, EMPLOYEE_COUNT);
                List<Employee> employees = new ArrayList<>(end - start);
                for (int index = start; index < end; index++) {
                    employees.add(createEmployee(index, random, departments));
                }
                executeChunkWithRetry(end, employees);
                log.info("Seeded {}/{} employees", end, EMPLOYEE_COUNT);
            }
        } catch (Exception exception) {
            log.error("Demo data seed failed", exception);
        } finally {
            log.info("Finished deterministic demo data seed");
        }
    }

    private long count(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    private void executeChunkWithRetry(int end, List<Employee> employees) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    insertEmployees(employees);
                    insertSalaryHistory(employees);
                });
                return;
            } catch (RuntimeException exception) {
                if (!isRetryable(exception) || attempt == MAX_ATTEMPTS) {
                    log.error("Demo data seed failed for chunk {}/{} on attempt {}/{}", end, EMPLOYEE_COUNT,
                            attempt, MAX_ATTEMPTS, exception);
                    throw exception;
                }
                long delaySeconds = 1L << (attempt - 1);
                log.warn("Transient failure seeding chunk {}/{}; retrying in {}s (attempt {}/{})",
                        end, EMPLOYEE_COUNT, delaySeconds, attempt + 1, MAX_ATTEMPTS, exception);
                try {
                    Thread.sleep(delaySeconds * 1_000L);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Demo data seed interrupted between retries", interruptedException);
                }
            }
        }
    }

    private void insertEmployees(List<Employee> employees) {
        jdbcTemplate.batchUpdate(EMPLOYEE_INSERT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int index) throws SQLException {
                Employee employee = employees.get(index);
                statement.setObject(1, employee.getId());
                statement.setString(2, employee.getEmployeeNumber());
                statement.setString(3, employee.getFirstName());
                statement.setString(4, employee.getLastName());
                statement.setString(5, employee.getEmail());
                statement.setObject(6, employee.getDepartment().getId());
                statement.setString(7, employee.getJobTitle());
                statement.setString(8, employee.getCountry());
                statement.setString(9, employee.getLocation());
                statement.setString(10, employee.getStatus().name());
                statement.setObject(11, employee.getHireDate());
                statement.setString(12, employee.getCurrency());
                statement.setBigDecimal(13, employee.getBaseSalary());
                statement.setBigDecimal(14, employee.getBonus());
                statement.setObject(15, employee.getCreatedAt());
                statement.setObject(16, employee.getUpdatedAt());
                statement.setLong(17, 0L);
            }

            @Override
            public int getBatchSize() {
                return employees.size();
            }
        });
    }

    private void insertSalaryHistory(List<Employee> employees) {
        jdbcTemplate.batchUpdate(SALARY_HISTORY_INSERT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int index) throws SQLException {
                Employee employee = employees.get(index);
                statement.setObject(1, deterministicId("salary-history-" + employee.getEmployeeNumber()));
                statement.setObject(2, employee.getId());
                statement.setString(3, employee.getCurrency());
                statement.setBigDecimal(4, employee.getBaseSalary());
                statement.setBigDecimal(5, employee.getBonus());
                statement.setObject(6, employee.getHireDate());
                statement.setString(7, "Initial compensation");
                statement.setObject(8, SEEDED_AT);
            }

            @Override
            public int getBatchSize() {
                return employees.size();
            }
        });
    }

    private boolean isRetryable(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof DataAccessResourceFailureException
                    || cause instanceof TransientDataAccessException) {
                return true;
            }
            if (cause instanceof SQLException sqlException && sqlException.getSQLState() != null
                    && sqlException.getSQLState().startsWith("08")) {
                return true;
            }
        }
        return false;
    }

    @PreDestroy
    void shutdown() {
        stopping = true;
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
