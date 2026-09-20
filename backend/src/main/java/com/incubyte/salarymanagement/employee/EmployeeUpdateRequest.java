package com.incubyte.salarymanagement.employee;

import jakarta.validation.constraints.*;

public record EmployeeUpdateRequest(
        @NotNull Long version,
        @NotBlank @Size(max = 30) String employeeNumber,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 150) String jobTitle,
        @NotBlank @Size(max = 100) String country,
        @NotBlank @Size(max = 150) String location,
        @NotNull EmploymentStatus status,
        @NotNull @PastOrPresent java.time.LocalDate hireDate,
        @NotBlank @SupportedCurrency String currency,
        @NotNull @DecimalMin("0.00") java.math.BigDecimal baseSalary,
        @NotNull @DecimalMin("0.00") java.math.BigDecimal bonus,
        @Size(max = 500) String salaryChangeReason) {
    EmployeeRequest asEmployeeRequest() {
        return new EmployeeRequest(employeeNumber, firstName, lastName, email, department, jobTitle, country,
                location, status, hireDate, currency, baseSalary, bonus, salaryChangeReason);
    }
}
