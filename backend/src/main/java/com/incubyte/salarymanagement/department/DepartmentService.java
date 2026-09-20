package com.incubyte.salarymanagement.department;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll(Sort.by("name")).stream().map(DepartmentResponse::from).toList();
    }
}
