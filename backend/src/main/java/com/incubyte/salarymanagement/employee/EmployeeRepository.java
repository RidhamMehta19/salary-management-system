package com.incubyte.salarymanagement.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {
    @Override
    @EntityGraph(attributePaths = "department")
    Page<Employee> findAll(org.springframework.data.jpa.domain.Specification<Employee> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "department")
    Optional<Employee> findById(UUID id);
    boolean existsByEmployeeNumberIgnoreCase(String employeeNumber);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    boolean existsByEmployeeNumberIgnoreCaseAndIdNot(String employeeNumber, UUID id);
    long countByStatus(EmploymentStatus status);
}
