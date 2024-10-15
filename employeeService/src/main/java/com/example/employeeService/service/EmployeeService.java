package com.example.employeeService.service;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.model.Employee;
import com.example.employeeService.repository.EmployeeRepository;
import jakarta.validation.ValidationException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee createEmployee(EmployeeCreationRequest request) {
       // validateEmployeeRequest(request);

        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPosition(request.getPosition());
        return employeeRepository.save(employee);
    }

    public Employee getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.orElse(null);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee updateEmployee(Long id, EmployeeCreationRequest request) {
        //validateEmployeeRequest(request);
        Employee employee = getEmployeeById(id);
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPosition(request.getPosition());
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    private void validateEmployeeRequest(EmployeeCreationRequest request) {
        if (request == null) {
            throw new ValidationException("Employee creation request cannot be null");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Employee name cannot be null or blank");
        }

        if (request.getName().length() < 2 || request.getName().length() > 50) {
            throw new ValidationException("Employee name must be between 2 and 50 characters");
        }

        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }

        if (request.getPosition() == null || request.getPosition().isBlank()) {
            throw new ValidationException("Position cannot be null or blank");
        }
    }
}
