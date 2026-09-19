package com.incubyte.salarymanagement.department;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
class DepartmentDataInitializer implements CommandLineRunner {
    private final DepartmentRepository departmentRepository;

    DepartmentDataInitializer(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) {
        DepartmentCatalog.NAMES.forEach(name -> departmentRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> departmentRepository.save(new Department(name))));
    }
}
