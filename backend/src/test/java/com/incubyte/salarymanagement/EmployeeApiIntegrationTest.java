package com.incubyte.salarymanagement;

import com.incubyte.salarymanagement.employee.EmployeeRepository;
import com.incubyte.salarymanagement.salary.SalaryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeApiIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private SalaryHistoryRepository salaryHistoryRepository;

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
