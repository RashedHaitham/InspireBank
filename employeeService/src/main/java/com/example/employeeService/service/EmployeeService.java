package com.example.employeeService.service;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.model.Employee;
import com.example.employeeService.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    public void saveEmployees(MultipartFile file) throws IOException {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                 String[] data = line.split(",");
                if (data.length == 3) {  // Assuming 3 columns: name, email, position
                    Employee employee = new Employee(null, data[1],data[0], data[2]);
                    employees.add(employee);
                }
            }
        }
        employeeRepository.saveAll(employees);

    }

    @Cacheable(key = "#id",value = "Employee")
    public Employee getEmployeeById(Long id) {
        System.out.println("no cache, getting employee "+id+ " form database...");
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.orElse(null);
    }

    @Cacheable(value = "Employee")
    public Page<Employee> getAllEmployees(int page, int size) {
        System.out.println("No cache, getting employees from the database...");
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(pageable);
    }

    public Employee updateEmployee(Long id, EmployeeCreationRequest request) {
        //validateEmployeeRequest(request);
        Employee employee = getEmployeeById(id);
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPosition(request.getPosition());
        return employeeRepository.save(employee);
    }

    @CacheEvict(key = "#id",value = "Employee")
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

//    private void validateEmployeeRequest(EmployeeCreationRequest request) {
//        if (request == null) {
//            throw new ValidationException("Employee creation request cannot be null");
//        }
//
//        if (request.getName() == null || request.getName().isBlank()) {
//            throw new ValidationException("Employee name cannot be null or blank");
//        }
//
//        if (request.getName().length() < 2 || request.getName().length() > 50) {
//            throw new ValidationException("Employee name must be between 2 and 50 characters");
//        }
//
//        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
//            throw new ValidationException("Invalid email format");
//        }
//
//        if (request.getPosition() == null || request.getPosition().isBlank()) {
//            throw new ValidationException("Position cannot be null or blank");
//        }
//    }
}
