package com.incubyte.salarymanagement.employee;

import com.incubyte.salarymanagement.common.PageResponse;
import com.incubyte.salarymanagement.common.ResourceNotFoundException;
import com.incubyte.salarymanagement.department.Department;
import com.incubyte.salarymanagement.department.DepartmentRepository;
import com.incubyte.salarymanagement.salary.SalaryHistory;
import com.incubyte.salarymanagement.salary.SalaryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private SalaryHistoryRepository salaryHistoryRepository;
    @Captor private ArgumentCaptor<SalaryHistory> historyCaptor;
    private EmployeeService employeeService;
    private Department engineering;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, departmentRepository, salaryHistoryRepository);
        engineering = new Department("Engineering");
    }

    @Test
    void createsEmployeeAndInitialSalaryHistory() {
        EmployeeRequest request = request("EMP-10001", "new.hire@example.org", new BigDecimal("90000"), new BigDecimal("10000"));
        when(departmentRepository.findByNameIgnoreCase("Engineering")).thenReturn(Optional.of(engineering));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse response = employeeService.create(request);

        assertThat(response.employeeNumber()).isEqualTo("EMP-10001");
        assertThat(response.totalCompensation()).isEqualByComparingTo("100000");
        verify(salaryHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getChangeReason()).isEqualTo("Initial compensation");
        assertThat(historyCaptor.getValue().getTotalCompensation()).isEqualByComparingTo("100000");
    }

    @Test
    void recordsHistoryOnlyWhenCompensationChanges() {
        UUID id = UUID.randomUUID();
        Employee employee = employee("EMP-00001", "original@example.org", new BigDecimal("90000"), new BigDecimal("5000"));
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(departmentRepository.findByNameIgnoreCase("Engineering")).thenReturn(Optional.of(engineering));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.update(id, request("EMP-00001", "original@example.org", new BigDecimal("97500"), new BigDecimal("7500")));

        verify(salaryHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getBaseSalary()).isEqualByComparingTo("97500");
        assertThat(historyCaptor.getValue().getBonus()).isEqualByComparingTo("7500");
    }

    @Test
    void doesNotRecordSalaryHistoryForNonCompensationEdit() {
        UUID id = UUID.randomUUID();
        Employee employee = employee("EMP-00001", "original@example.org", new BigDecimal("90000"), new BigDecimal("5000"));
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(departmentRepository.findByNameIgnoreCase("Engineering")).thenReturn(Optional.of(engineering));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.update(id, request("EMP-00001", "original@example.org", new BigDecimal("90000"), new BigDecimal("5000")));

        verify(salaryHistoryRepository, never()).save(any(SalaryHistory.class));
    }

    @Test
    void returnsServerSidePageMetadata() {
        Employee employee = employee("EMP-00001", "one@example.org", new BigDecimal("90000"), new BigDecimal("5000"));
        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee), org.springframework.data.domain.PageRequest.of(1, 10), 31));

        PageResponse<EmployeeResponse> result = employeeService.findEmployees(1, 10, "lastName,desc", "Ada", null, null, null);

        assertThat(result.content()).hasSize(1);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(31);
        assertThat(result.totalPages()).isEqualTo(4);
    }

    @Test
    void reportsMissingEmployee() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private Employee employee(String employeeNumber, String email, BigDecimal baseSalary, BigDecimal bonus) {
        return new Employee(employeeNumber, "Ada", "Lovelace", email, engineering, "Software Engineer", "United States",
                "New York", EmploymentStatus.ACTIVE, LocalDate.of(2020, 1, 10), "USD", baseSalary, bonus);
    }

    private EmployeeRequest request(String employeeNumber, String email, BigDecimal baseSalary, BigDecimal bonus) {
        return new EmployeeRequest(employeeNumber, "Ada", "Lovelace", email, "Engineering", "Software Engineer",
                "United States", "New York", EmploymentStatus.ACTIVE, LocalDate.of(2020, 1, 10), "USD",
                baseSalary, bonus, "Annual review");
    }
}
