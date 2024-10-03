package com.example.employeeService.service;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.model.Employee;
import com.example.employeeService.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        // Arrange
        EmployeeCreationRequest request = new EmployeeCreationRequest("John Doe", "john.doe@example.com", "Developer");
        Employee employee = new Employee(1L, "John Doe", "john.doe@example.com", "Developer");

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        Employee createdEmployee = employeeService.createEmployee(request);

        // Assert
        assertNotNull(createdEmployee);
        assertEquals("John Doe", createdEmployee.getName());
        assertEquals("john.doe@example.com", createdEmployee.getEmail());
        assertEquals("Developer", createdEmployee.getPosition());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_shouldReturnEmployeeIfExists() {
        // Arrange
        Long employeeId = 1L;
        Employee employee = new Employee(1L, "John Doe", "john.doe@example.com", "Developer");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Act
        Employee foundEmployee = employeeService.getEmployeeById(employeeId);

        // Assert
        assertNotNull(foundEmployee);
        assertEquals("John Doe", foundEmployee.getName());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getEmployeeById_shouldReturnNullIfEmployeeDoesNotExist() {
        // Arrange
        Long employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act
        Employee foundEmployee = employeeService.getEmployeeById(employeeId);

        // Assert
        assertNull(foundEmployee);
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        // Arrange
        Employee employee1 = new Employee(1L, "John Doe", "john.doe@example.com", "Developer");
        Employee employee2 = new Employee(2L, "Jane Doe", "jane.doe@example.com", "Manager");

        when(employeeRepository.findAll()).thenReturn(List.of(employee1, employee2));

        // Act
        List<Employee> employees = employeeService.getAllEmployees();

        // Assert
        assertEquals(2, employees.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_shouldReturnUpdatedEmployee() {
        // Arrange
        Long employeeId = 1L;
        Employee existingEmployee = new Employee(1L, "John Doe", "john.doe@example.com", "Developer");
        EmployeeCreationRequest request = new EmployeeCreationRequest("John Doe Updated", "john.updated@example.com", "Senior Developer");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);

        // Act
        Employee updatedEmployee = employeeService.updateEmployee(employeeId, request);

        // Assert
        assertNotNull(updatedEmployee);
        assertEquals("John Doe Updated", updatedEmployee.getName());
        assertEquals("john.updated@example.com", updatedEmployee.getEmail());
        assertEquals("Senior Developer", updatedEmployee.getPosition());
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee() {
        // Arrange
        Long employeeId = 1L;

        doNothing().when(employeeRepository).deleteById(employeeId);

        // Act
        employeeService.deleteEmployee(employeeId);

        // Assert
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }
}
