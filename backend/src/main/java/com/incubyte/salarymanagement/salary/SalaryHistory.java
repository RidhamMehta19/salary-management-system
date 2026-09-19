package com.incubyte.salarymanagement.salary;

import com.incubyte.salarymanagement.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "salary_history")
public class SalaryHistory {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "base_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseSalary;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal bonus;
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    @Column(name = "change_reason", nullable = false, length = 500)
    private String changeReason;
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected SalaryHistory() {
    }

    public SalaryHistory(Employee employee, String currency, BigDecimal baseSalary, BigDecimal bonus,
                         LocalDate effectiveDate, String changeReason) {
        this.id = UUID.randomUUID();
        this.employee = employee;
        this.currency = currency;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.effectiveDate = effectiveDate;
        this.changeReason = changeReason;
    }

    @PrePersist
    void onCreate() { recordedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getCurrency() { return currency; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public BigDecimal getBonus() { return bonus; }
    public BigDecimal getTotalCompensation() { return baseSalary.add(bonus); }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getChangeReason() { return changeReason; }
    public Instant getRecordedAt() { return recordedAt; }
}
