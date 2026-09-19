package com.incubyte.salarymanagement.employee;

import com.incubyte.salarymanagement.common.GlobalExceptionHandler;
import com.incubyte.salarymanagement.common.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private EmployeeService employeeService;

    @Test
    void returnsStructuredValidationErrors() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void returnsNotFoundErrorWithoutInternalDetails() throws Exception {
        UUID id = UUID.randomUUID();
        when(employeeService.findById(id)).thenThrow(new ResourceNotFoundException("Employee with id " + id + " was not found"));

        mockMvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee with id " + id + " was not found"));
    }
}
