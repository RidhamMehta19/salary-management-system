package com.incubyte.salarymanagement.employee;

import jakarta.validation.constraints.NotNull;

public record EmployeeStatusRequest(@NotNull EmploymentStatus status) {
}
