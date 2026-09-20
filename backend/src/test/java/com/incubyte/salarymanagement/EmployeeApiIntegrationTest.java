package com.incubyte.salarymanagement;

import com.incubyte.salarymanagement.employee.EmployeeRepository;
import com.incubyte.salarymanagement.salary.SalaryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class EmployeeApiIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
    @Autowired private MockMvc mockMvc;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private SalaryHistoryRepository salaryHistoryRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearEmployeeData() {
        salaryHistoryRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void createsFiltersAndDeactivatesAnEmployee() throws Exception {
        String request = """
                {"employeeNumber":"EMP-TEST-1","firstName":"Grace","lastName":"Hopper",
                 "email":"grace.hopper@example.org","department":"Engineering","jobTitle":"Admiral",
                 "country":"United States","location":"New York","status":"ACTIVE","hireDate":"2020-01-15",
                 "currency":"USD","baseSalary":120000,"bonus":15000,"salaryChangeReason":"Initial offer"}
                """;

        String response = mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCompensation").value(135000))
                .andReturn().getResponse().getContentAsString();
        String id = response.replaceAll(".*\\\"id\\\":\\\"([^\\\"]+).*", "$1");

        mockMvc.perform(get("/api/employees").param("search", "Hopper").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].employeeNumber").value("EMP-TEST-1"));

        mockMvc.perform(patch("/api/employees/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(get("/api/employees/{id}/salary-history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeReason").value("Initial compensation"));
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeEmployees").value(0));
        mockMvc.perform(get("/api/dashboard/by-department")).andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/by-country")).andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/salary-distribution")).andExpect(status().isOk());
        assertThat(employeeRepository.count()).isEqualTo(1);
    }

    @Test
    void returnsClientErrorsForMalformedAndInvalidRequests() throws Exception {
        mockMvc.perform(get("/api/employees/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee id must be a valid UUID"));
        mockMvc.perform(get("/api/employees").param("status", "BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status must be ACTIVE, INACTIVE, or ON_LEAVE"));
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeNumber\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.employeeNumber").exists());
        mockMvc.perform(get("/api/employees/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictsForDuplicateEmployeeNumberAndEmail() throws Exception {
        String first = employeeRequest("EMP-CONFLICT-1", "first@example.org");
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-CONFLICT-1", "second@example.org")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Employee ID already exists"));
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-CONFLICT-2", "first@example.org")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email address already exists"));
    }

    @Test
    void rejectsCaseVariantDuplicatesAtDatabaseBoundary() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-CASE-1", "case@example.org")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("emp-case-1", "other@example.org")))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-CASE-2", "CASE@EXAMPLE.ORG")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsStaleUpdateWithConflict() throws Exception {
        String response = mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-STALE", "stale@example.org")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = response.replaceAll(".*\\\"id\\\":\\\"([^\\\"]+).*", "$1");
        String update = employeeRequest("EMP-STALE", "stale@example.org").replace("\"version\":", "\"version\":");

        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(update.replace("}", ",\"version\":0}")))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(update.replace("}", ",\"version\":0}")))
                .andExpect(status().isConflict());
    }

    @Test
    void validatesPagingSortingAndDepartment() throws Exception {
        mockMvc.perform(get("/api/employees").param("page", "-1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/employees").param("size", "101")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/employees").param("sort", "notAField,asc")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/employees").param("sort", "lastName,sideways")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-DEPT", "department@example.org").replace("Engineering", "Unknown")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usesIdAsTieBreakerForDeterministicPagination() throws Exception {
        String first = employeeRequest("EMP-TIE-1", "tie-1@example.org")
                .replace("Grace", "Same").replace("Hopper", "Tie");
        String second = employeeRequest("EMP-TIE-2", "tie-2@example.org")
                .replace("Grace", "Same").replace("Hopper", "Tie");
        String firstId = mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":\\\"([^\\\"]+).*", "$1");
        String secondId = mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(second))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":\\\"([^\\\"]+).*", "$1");

        String pageZero = mockMvc.perform(get("/api/employees").param("sort", "lastName,asc").param("size", "1"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String pageOne = mockMvc.perform(get("/api/employees").param("sort", "lastName,asc").param("size", "1").param("page", "1"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String returnedZero = pageZero.replaceAll(".*\\\"id\\\":\\\"([^\\\"]+).*", "$1");
        String returnedOne = pageOne.replaceAll(".*\\\"id\\\":\\\"([^\\\"]+).*", "$1");
        assertThat(List.of(returnedZero, returnedOne)).containsExactlyElementsOf(
                java.util.stream.Stream.of(firstId, secondId).sorted().toList());
    }

    @Test
    void rejectsFutureHireDateAndUnsupportedCurrencies() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-FUTURE", "future@example.org").replace("2020-01-15", "2999-01-15")))
                .andExpect(status().isBadRequest());
        for (String currency : new String[]{"XXX", "ZZZ"}) {
            mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                            .content(employeeRequest("EMP-" + currency, currency.toLowerCase() + "@example.org")
                                    .replace("\"currency\":\"USD\"", "\"currency\":\"" + currency + "\"")))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-INR-OK", "inr-ok@example.org")
                                .replace("\"currency\":\"USD\"", "\"currency\":\"INR\"")))
                .andExpect(status().isCreated());
    }

    @Test
    void databaseChecksRejectInvalidEmploymentStatusAndCurrency() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbcTemplate.update(
                        "insert into employees (id, employee_number, first_name, last_name, email, department_id, job_title, country, location, employment_status, hire_date, currency, base_salary, bonus, created_at, updated_at, version) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), 0)",
                        java.util.UUID.randomUUID(), "EMP-DB", "A", "B", "db@example.org", jdbcTemplate.queryForObject("select id from departments limit 1", java.util.UUID.class), "Job", "US", "NY", "INVALID", java.time.LocalDate.now(), "USD", 1, 1))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                        "insert into employees (id, employee_number, first_name, last_name, email, department_id, job_title, country, location, employment_status, hire_date, currency, base_salary, bonus, created_at, updated_at, version) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), 0)",
                        java.util.UUID.randomUUID(), "EMP-DB-2", "A", "B", "db-2@example.org", jdbcTemplate.queryForObject("select id from departments limit 1", java.util.UUID.class), "Job", "US", "NY", "ACTIVE", java.time.LocalDate.now(), "XXX", 1, 1))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void groupsSalaryDistributionUsingCurrencySpecificBands() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-USD-BAND", "usd.band@example.org")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequest("EMP-INR-BAND", "inr.band@example.org")
                                .replace("\"currency\":\"USD\",\"baseSalary\":120000", "\"currency\":\"INR\",\"baseSalary\":12000000")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/salary-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.currency == 'USD' && @.band == '100k–150k')].employeeCount").value(1))
                .andExpect(jsonPath("$[?(@.currency == 'INR' && @.band == '8.2M–12.3M')].employeeCount").value(1));
    }

    private String employeeRequest(String employeeNumber, String email) {
        return """
                {"employeeNumber":"%s","firstName":"Grace","lastName":"Hopper",
                 "email":"%s","department":"Engineering","jobTitle":"Admiral",
                 "country":"United States","location":"New York","status":"ACTIVE","hireDate":"2020-01-15",
                 "currency":"USD","baseSalary":120000,"bonus":15000}
                """.formatted(employeeNumber, email);
    }
}
