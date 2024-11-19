package com.example.employeeService.controller;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.model.Employee;
import com.example.employeeService.service.EmployeeService;
import graphql.schema.DataFetchingEnvironment;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class EmployeeGraphQLController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeGraphQLController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    @QueryMapping
    @Timed(value = "employeeGetById", description = "Time taken to get an employee by ID")
    public Employee getEmployeeById(@Argument Long id) {
        return employeeService.getEmployeeById(id);
    }

    @QueryMapping
    @Timed(value = "employeeGetAll", description = "Time taken to get all employees")
    public Page<Employee> getAllEmployees(
            @Argument int page,
            @Argument int size) {
        Page<Employee> employees = employeeService.getAllEmployees(page, size);
        if (employees.isEmpty()) {
            return null;
        }
        return employees;
    }

    @MutationMapping
    @Timed(value = "employeeCreate", description = "Time taken to create an employee")
    public Employee createEmployee(@Argument EmployeeCreationRequest input) {
        return employeeService.createEmployee(input);
    }

    @MutationMapping
    @Timed(value = "employeeUpdate", description = "Time taken to update an employee")
    public Employee updateEmployee(@Argument Long id,@Argument EmployeeCreationRequest input) {
        return employeeService.updateEmployee(id, input);
    }

    @MutationMapping
    @Timed(value = "employeeDelete", description = "Time taken to delete an employee")
    public Boolean deleteEmployee(@Argument Long id) {
        employeeService.deleteEmployee(id);
        return true;
    }

    @MutationMapping
    public String uploadPayrollFile(DataFetchingEnvironment environment) throws IOException {
            MultipartFile file = environment.getArgument("file");
            if (file.isEmpty()) {
                throw new RuntimeException("Please upload a valid file.");
            }
            employeeService.saveEmployees(file);
            return "Payroll file uploaded and saved successfully.";
        }
}
