package com.incubyte.salarymanagement.employee;

import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

final class EmployeeSpecifications {
    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(String search, String department, String country, EmploymentStatus status) {
        return Specification.allOf(
                containsSearch(search),
                equalsIgnoreCase("department", department),
                equalsIgnoreCase("country", country),
                hasStatus(status)
        );
    }

    private static Specification<Employee> containsSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("firstName")), pattern),
                builder.like(builder.lower(root.get("lastName")), pattern),
                builder.like(builder.lower(root.get("employeeNumber")), pattern),
                builder.like(builder.lower(builder.concat(builder.concat(root.get("firstName"), " "), root.get("lastName"))), pattern)
        );
    }

    private static Specification<Employee> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, builder) -> {
            if ("department".equals(field)) {
                return builder.equal(builder.lower(root.join("department").get("name")), value.trim().toLowerCase(Locale.ROOT));
            }
            return builder.equal(builder.lower(root.get(field)), value.trim().toLowerCase(Locale.ROOT));
        };
    }

    private static Specification<Employee> hasStatus(EmploymentStatus status) {
        return status == null ? null : (root, query, builder) -> builder.equal(root.get("status"), status);
    }
}
