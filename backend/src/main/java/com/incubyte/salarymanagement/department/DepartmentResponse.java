package com.incubyte.salarymanagement.department;

import java.util.UUID;

public record DepartmentResponse(UUID id, String name) {
    static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
