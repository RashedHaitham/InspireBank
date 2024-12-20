package com.example.employeeService.controller;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.model.Employee;
import com.example.employeeService.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() throws Exception {
        // Arrange
        EmployeeCreationRequest request = new EmployeeCreationRequest("Rashed", "rashed@example.com", "Developer");
        Employee employee = new Employee(1L, "rashed@example.com", "Rashed", "Developer");

        when(employeeService.createEmployee(any(EmployeeCreationRequest.class))).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rashed"))
                .andExpect(jsonPath("$.email").value("rashed@example.com"))
                .andExpect(jsonPath("$.position").value("Developer"));
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() throws Exception {
        // Arrange
        Long employeeId = 1L;
        Employee employee = new Employee(1L, "john.doe@example.com", "John Doe", "Developer");

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(get("/api/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.position").value("Developer"));
    }

    @Test
    void getAllEmployees_shouldReturnListOfEmployees() throws Exception {
        Employee employee1 = new Employee(1L, "rashed@example.com", "rashed", "Developer");
        Employee employee2 = new Employee(2L, "dova@example.com", "dova", "Manager");
        List<Employee> employeeList = List.of(employee1, employee2);

        Page<Employee> employeePage = new PageImpl<>(employeeList, PageRequest.of(0, 5), employeeList.size());

        when(employeeService.getAllEmployees(0, 5)).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employee?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("rashed"))
                .andExpect(jsonPath("$.content[0].email").value("rashed@example.com"))
                .andExpect(jsonPath("$.content[0].position").value("Developer"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("dova"))
                .andExpect(jsonPath("$.content[1].email").value("dova@example.com"))
                .andExpect(jsonPath("$.content[1].position").value("Manager"));
    }

    @Test
    void updateEmployee_shouldReturnUpdatedEmployee() throws Exception {
        // Arrange
        Long employeeId = 1L;
        EmployeeCreationRequest request = new EmployeeCreationRequest("Rashed Updated", "rashed.updated@example.com", "Senior Developer");
        Employee updatedEmployee = new Employee(1L, "rashed.updated@example.com", "Rashed Updated", "Senior Developer");

        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeCreationRequest.class))).thenReturn(updatedEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employee/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rashed Updated"))
                .andExpect(jsonPath("$.email").value("rashed.updated@example.com"))
                .andExpect(jsonPath("$.position").value("Senior Developer"));
    }

    @Test
    void deleteEmployee_shouldReturnNoContent() throws Exception {
        // Arrange
        Long employeeId = 1L;
        doNothing().when(employeeService).deleteEmployee(employeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/employee/{id}", employeeId))
                .andExpect(status().isNoContent());
    }
}
