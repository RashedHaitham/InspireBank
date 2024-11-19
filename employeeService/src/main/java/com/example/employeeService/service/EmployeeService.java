package com.example.employeeService.service;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.exception.EmployeeNotFoundException;
import com.example.employeeService.model.Employee;
import com.example.employeeService.repository.EmployeeRepository;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private static final String EMPLOYEE_CACHE = "Employee";

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @CachePut(value = EMPLOYEE_CACHE, key = "#result.id")
    public Employee createEmployee(EmployeeCreationRequest request) {
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPosition(request.getPosition());
        return employeeRepository.save(employee);
    }

    @CachePut(value = EMPLOYEE_CACHE, key = "#result.id")
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
                if (data.length == 3) {  // 3 columns: name, email, position
                    Employee employee = new Employee(null, data[1], data[0], data[2]);
                    employees.add(employee);
                }
            }
        }
        employeeRepository.saveAll(employees);
    }

    @Timed("employeeCacheGet")
    @Cacheable(key = "#id", value = EMPLOYEE_CACHE)
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        System.out.println("No cache, getting employee " + id + " from the database...");
        return employeeRepository.findById(id).orElse(null);
    }

    @Timed("employeeCacheGetAll")
    @Cacheable(value = EMPLOYEE_CACHE, key = "#page + '-' + #size")
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(int page, int size) {
        System.out.println("No cache, getting employees from the database...");
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findAll(pageable);
    }

    @CachePut(value = EMPLOYEE_CACHE, key = "#id")
    public Employee updateEmployee(Long id, EmployeeCreationRequest request) {
        Employee employee = getEmployeeById(id);
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPosition(request.getPosition());
        return employeeRepository.save(employee);
    }

    @CacheEvict(value = EMPLOYEE_CACHE, key = "#id")
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
    }
}