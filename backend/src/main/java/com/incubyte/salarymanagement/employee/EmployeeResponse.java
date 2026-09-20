package com.incubyte.salarymanagement.employee;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(UUID id, String employeeNumber, String firstName, String lastName,
                               String email, String department, String jobTitle, String country,
                               String location, EmploymentStatus status, LocalDate hireDate,
                               String currency, BigDecimal baseSalary, BigDecimal bonus,
                               BigDecimal totalCompensation, Instant createdAt, Instant updatedAt, Long version) {
    static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getEmployeeNumber(), employee.getFirstName(),
                employee.getLastName(), employee.getEmail(), employee.getDepartment().getName(), employee.getJobTitle(),
                employee.getCountry(), employee.getLocation(), employee.getStatus(), employee.getHireDate(),
                employee.getCurrency(), employee.getBaseSalary(), employee.getBonus(), employee.getTotalCompensation(),
                employee.getCreatedAt(), employee.getUpdatedAt(), employee.getVersion());
    }
}
