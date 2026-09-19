package com.incubyte.salarymanagement.employee;

import com.incubyte.salarymanagement.common.PageResponse;
import com.incubyte.salarymanagement.salary.SalaryHistoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(required = false) String search,
                                               @RequestParam(required = false) String department,
                                               @RequestParam(required = false) String country,
                                               @RequestParam(required = false) EmploymentStatus status) {
        return employeeService.findEmployees(page, size, sort, search, department, country, status);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable UUID id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable UUID id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public EmployeeResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody EmployeeStatusRequest request) {
        return employeeService.updateStatus(id, request);
    }

    @GetMapping("/{id}/salary-history")
    public List<SalaryHistoryResponse> salaryHistory(@PathVariable UUID id) {
        return employeeService.findSalaryHistory(id);
    }
}
