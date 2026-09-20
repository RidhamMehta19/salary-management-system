package com.incubyte.salarymanagement.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank @Size(max = 30) String employeeNumber,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 150) String jobTitle,
        @NotBlank @Size(max = 100) String country,
        @NotBlank @Size(max = 150) String location,
        @NotNull EmploymentStatus status,
        @NotNull @PastOrPresent LocalDate hireDate,
        @NotBlank @SupportedCurrency String currency,
        @NotNull @DecimalMin(value = "0.00") BigDecimal baseSalary,
        @NotNull @DecimalMin(value = "0.00") BigDecimal bonus,
        @Size(max = 500) String salaryChangeReason
) {
}
