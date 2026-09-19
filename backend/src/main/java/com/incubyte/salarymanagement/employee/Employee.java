package com.incubyte.salarymanagement.employee;

import com.incubyte.salarymanagement.department.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    private UUID id;
    @Column(name = "employee_number", nullable = false, unique = true, length = 30)
    private String employeeNumber;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;
    @Column(nullable = false, length = 100)
    private String country;
    @Column(nullable = false, length = 150)
    private String location;
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus status;
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "base_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseSalary;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal bonus;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Employee() {
    }

    public Employee(String employeeNumber, String firstName, String lastName, String email,
                    Department department, String jobTitle, String country, String location,
                    EmploymentStatus status, LocalDate hireDate, String currency,
                    BigDecimal baseSalary, BigDecimal bonus) {
        this.id = UUID.randomUUID();
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.jobTitle = jobTitle;
        this.country = country;
        this.location = location;
        this.status = status;
        this.hireDate = hireDate;
        this.currency = currency;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
    }

    public void update(String firstName, String lastName, String email, Department department,
                       String jobTitle, String country, String location, EmploymentStatus status,
                       LocalDate hireDate, String currency, BigDecimal baseSalary, BigDecimal bonus) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.jobTitle = jobTitle;
        this.country = country;
        this.location = location;
        this.status = status;
        this.hireDate = hireDate;
        this.currency = currency;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
    }

    public void changeStatus(EmploymentStatus status) { this.status = status; }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Department getDepartment() { return department; }
    public String getJobTitle() { return jobTitle; }
    public String getCountry() { return country; }
    public String getLocation() { return location; }
    public EmploymentStatus getStatus() { return status; }
    public LocalDate getHireDate() { return hireDate; }
    public String getCurrency() { return currency; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public BigDecimal getBonus() { return bonus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public BigDecimal getTotalCompensation() { return baseSalary.add(bonus); }
}
