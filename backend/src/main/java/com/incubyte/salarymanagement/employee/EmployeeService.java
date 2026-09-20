package com.incubyte.salarymanagement.employee;

import com.incubyte.salarymanagement.common.DuplicateResourceException;
import com.incubyte.salarymanagement.common.PageResponse;
import com.incubyte.salarymanagement.common.ResourceNotFoundException;
import com.incubyte.salarymanagement.department.Department;
import com.incubyte.salarymanagement.department.DepartmentRepository;
import com.incubyte.salarymanagement.salary.SalaryHistory;
import com.incubyte.salarymanagement.salary.SalaryHistoryRepository;
import com.incubyte.salarymanagement.salary.SalaryHistoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private static final Map<String, String> SORTABLE_FIELDS = Map.of(
            "employeeNumber", "employeeNumber", "firstName", "firstName", "lastName", "lastName",
            "email", "email", "country", "country", "status", "status", "hireDate", "hireDate",
            "baseSalary", "baseSalary", "updatedAt", "updatedAt");

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final Clock clock;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                           SalaryHistoryRepository salaryHistoryRepository, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.clock = clock;
    }

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                           SalaryHistoryRepository salaryHistoryRepository) {
        this(employeeRepository, departmentRepository, salaryHistoryRepository, Clock.systemUTC());
    }

    public PageResponse<EmployeeResponse> findEmployees(int page, int size, String sort, String search,
                                                        String department, String country, EmploymentStatus status) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return PageResponse.from(employeeRepository.findAll(
                EmployeeSpecifications.matching(search, department, country, status), pageable), EmployeeResponse::from);
    }

    public EmployeeResponse findById(UUID id) {
        return EmployeeResponse.from(getEmployee(id));
    }

    public List<SalaryHistoryResponse> findSalaryHistory(UUID id) {
        getEmployee(id);
        return salaryHistoryRepository.findByEmployeeIdOrderByEffectiveDateDescRecordedAtDesc(id).stream()
                .map(SalaryHistoryResponse::from).toList();
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        validateUniqueForCreate(request);
        Department department = findDepartment(request.department());
        Employee employee = new Employee(normalizeEmployeeNumber(request.employeeNumber()), normalize(request.firstName()),
                normalize(request.lastName()), normalizeEmail(request.email()), department, normalize(request.jobTitle()),
                normalize(request.country()), normalize(request.location()), request.status(), request.hireDate(),
                request.currency(), request.baseSalary(), request.bonus());
        Employee saved = employeeRepository.save(employee);
        salaryHistoryRepository.save(new SalaryHistory(saved, saved.getCurrency(), saved.getBaseSalary(), saved.getBonus(),
                saved.getHireDate(), "Initial compensation"));
        log.info("Created employee {}", saved.getEmployeeNumber());
        return EmployeeResponse.from(saved);
    }

    @Transactional
    public EmployeeResponse update(UUID id, EmployeeUpdateRequest updateRequest) {
        EmployeeRequest request = updateRequest.asEmployeeRequest();
        Employee employee = getEmployee(id);
        if (updateRequest.version() == null || employee.getVersion() == null
                ? updateRequest.version() != employee.getVersion()
                : !updateRequest.version().equals(employee.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(employee);
        }
        validateUniqueForUpdate(id, request);
        boolean compensationChanged = compensationChanged(employee, request);
        Department department = findDepartment(request.department());
        employee.update(normalizeEmployeeNumber(request.employeeNumber()), normalize(request.firstName()), normalize(request.lastName()), normalizeEmail(request.email()),
                department, normalize(request.jobTitle()), normalize(request.country()), normalize(request.location()),
                request.status(), request.hireDate(), request.currency(), request.baseSalary(), request.bonus());
        Employee saved = employeeRepository.save(employee);
        if (compensationChanged) {
            salaryHistoryRepository.save(new SalaryHistory(saved, saved.getCurrency(), saved.getBaseSalary(), saved.getBonus(),
                    // Salary changes take effect on the UTC calendar date when the update is committed.
                    LocalDate.now(clock), defaultReason(request.salaryChangeReason())));
            log.info("Updated compensation for employee {}", saved.getEmployeeNumber());
        } else {
            log.info("Updated employee {}", saved.getEmployeeNumber());
        }
        return EmployeeResponse.from(saved);
    }

    /** Compatibility overload for direct service callers; HTTP updates use the versioned DTO. */
    @Transactional
    public EmployeeResponse update(UUID id, EmployeeRequest request) {
        Employee current = getEmployee(id);
        return update(id, new EmployeeUpdateRequest(current.getVersion(), request.employeeNumber(), request.firstName(),
                request.lastName(), request.email(), request.department(), request.jobTitle(), request.country(),
                request.location(), request.status(), request.hireDate(), request.currency(), request.baseSalary(),
                request.bonus(), request.salaryChangeReason()));
    }

    @Transactional
    public EmployeeResponse updateStatus(UUID id, EmployeeStatusRequest request) {
        Employee employee = getEmployee(id);
        employee.changeStatus(request.status());
        Employee saved = employeeRepository.save(employee);
        log.info("Changed employee {} status to {}", saved.getEmployeeNumber(), saved.getStatus());
        return EmployeeResponse.from(saved);
    }

    private Employee getEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " was not found"));
    }

    private Department findDepartment(String departmentName) {
        return departmentRepository.findByNameIgnoreCase(normalize(departmentName))
                .orElseThrow(() -> new IllegalArgumentException("Unknown department: " + departmentName));
    }

    private void validateUniqueForCreate(EmployeeRequest request) {
        if (employeeRepository.existsByEmployeeNumberIgnoreCase(normalizeEmployeeNumber(request.employeeNumber()))) {
            throw new DuplicateResourceException("Employee ID already exists");
        }
        if (employeeRepository.existsByEmailIgnoreCase(normalizeEmail(request.email()))) {
            throw new DuplicateResourceException("Email address already exists");
        }
    }

    private void validateUniqueForUpdate(UUID id, EmployeeRequest request) {
        if (employeeRepository.existsByEmployeeNumberIgnoreCaseAndIdNot(normalizeEmployeeNumber(request.employeeNumber()), id)) {
            throw new DuplicateResourceException("Employee ID already exists");
        }
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(normalizeEmail(request.email()), id)) {
            throw new DuplicateResourceException("Email address already exists");
        }
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "lastName", "firstName", "id");
        }
        String[] parts = sort.split(",", 2);
        String field = SORTABLE_FIELDS.get(parts[0]);
        if (field == null) {
            throw new IllegalArgumentException("Unsupported sort field: " + parts[0]);
        }
        Sort.Direction direction = parts.length == 2 ? Sort.Direction.fromOptionalString(parts[1]).orElseThrow(
                () -> new IllegalArgumentException("Sort direction must be asc or desc")) : Sort.Direction.ASC;
        return Sort.by(direction, field).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private boolean compensationChanged(Employee employee, EmployeeRequest request) {
        return !employee.getCurrency().equals(request.currency())
                || employee.getBaseSalary().compareTo(request.baseSalary()) != 0
                || employee.getBonus().compareTo(request.bonus()) != 0;
    }

    private String defaultReason(String reason) {
        return reason == null || reason.isBlank() ? "Compensation updated" : reason.trim();
    }

    private String normalize(String value) { return value.trim(); }
    private String normalizeEmployeeNumber(String value) { return value.trim().toUpperCase(Locale.ROOT); }
    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
