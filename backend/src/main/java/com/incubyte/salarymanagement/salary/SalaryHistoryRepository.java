package com.incubyte.salarymanagement.salary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, UUID> {
    List<SalaryHistory> findByEmployeeIdOrderByEffectiveDateDescRecordedAtDesc(UUID employeeId);
}
