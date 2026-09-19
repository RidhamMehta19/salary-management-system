package com.incubyte.salarymanagement.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmployeeNumberIgnoreCase(String employeeNumber);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    boolean existsByEmployeeNumberIgnoreCaseAndIdNot(String employeeNumber, UUID id);
    long countByStatus(EmploymentStatus status);
}
