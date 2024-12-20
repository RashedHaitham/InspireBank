package com.example.employeeService.service;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.exception.EmployeeNotFoundException;
import com.example.employeeService.model.Employee;
import com.example.employeeService.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        EmployeeCreationRequest request = new EmployeeCreationRequest("Rashed", "rashed@example.com", "Developer");
        Employee employee = new Employee(null, "rashed@example.com", "Rashed", "Developer");

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee createdEmployee = employeeService.createEmployee(request);

        assertNotNull(createdEmployee);
        assertEquals("Rashed", createdEmployee.getName());
        assertEquals("rashed@example.com", createdEmployee.getEmail());
        assertEquals("Developer", createdEmployee.getPosition());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_shouldReturnEmployeeIfExists() {
        Long employeeId = 1L;
        Employee employee = new Employee(employeeId, "rashed@example.com", "Rashed", "Developer");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Employee foundEmployee = employeeService.getEmployeeById(employeeId);

        assertNotNull(foundEmployee);
        assertEquals(employeeId, foundEmployee.getId());
        assertEquals("Rashed", foundEmployee.getName());
        verify(employeeRepository, times(1)).findById(employeeId);
    }


    @Test
    void getEmployeeById_shouldThrowExceptionIfEmployeeDoesNotExist() {
        // Arrange
        Long employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));
        verify(employeeRepository, times(1)).findById(employeeId);
    }


    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        // Arrange
        Employee employee1 = new Employee(1L, "rashed@example.com", "Rashed", "Developer");
        Employee employee2 = new Employee(2L, "dova@example.com", "Dova", "Manager");
        List<Employee> employeeList = List.of(employee1, employee2);

        // Create a Page object with the list of employees
        Page<Employee> employeePage = new PageImpl<>(employeeList, PageRequest.of(0, 5), employeeList.size());

        // Mock the repository call to return a paginated result
        when(employeeRepository.findAll(PageRequest.of(0, 5))).thenReturn(employeePage);

        // Act
        Page<Employee> employees = employeeService.getAllEmployees(0, 5);

        // Assert
        assertEquals(2, employees.getContent().size());
        verify(employeeRepository, times(1)).findAll(PageRequest.of(0, 5));
    }

    @Test
    void updateEmployee_shouldReturnUpdatedEmployee() {
        // Arrange
        Long employeeId = 1L;
        Employee existingEmployee = new Employee(1L, "rashed@example.com", "Rashed", "Developer");
        EmployeeCreationRequest request = new EmployeeCreationRequest("Rashed Updated", "rashed.updated@example.com", "Senior Developer");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);

        // Act
        Employee updatedEmployee = employeeService.updateEmployee(employeeId, request);

        // Assert
        assertNotNull(updatedEmployee);
        assertEquals("Rashed Updated", updatedEmployee.getName());
        assertEquals("rashed.updated@example.com", updatedEmployee.getEmail());
        assertEquals("Senior Developer", updatedEmployee.getPosition());
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void deleteEmployee_shouldThrowExceptionIfEmployeeDoesNotExist() {
        Long employeeId = 1L;

        when(employeeRepository.existsById(employeeId)).thenReturn(false); // Employee does not exist

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));

        verify(employeeRepository, times(1)).existsById(employeeId); // Verify existence check
        verify(employeeRepository, times(0)).deleteById(employeeId); // Ensure delete is not called
    }

}
