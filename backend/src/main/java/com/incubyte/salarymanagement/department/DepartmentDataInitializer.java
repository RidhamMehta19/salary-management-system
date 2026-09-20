package com.incubyte.salarymanagement.department;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(1)
class DepartmentDataInitializer implements CommandLineRunner {
    private final DepartmentRepository departmentRepository;

    DepartmentDataInitializer(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Department> existing = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(department -> department.getName().toLowerCase(), Function.identity()));
        List<Department> missing = DepartmentCatalog.NAMES.stream()
                .filter(name -> !existing.containsKey(name.toLowerCase()))
                .map(Department::new).toList();
        departmentRepository.saveAll(missing);
    }
}
