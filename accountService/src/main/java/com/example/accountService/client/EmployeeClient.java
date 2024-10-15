package com.example.accountService.client;


import com.example.accountService.dto.EmployeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "employeeService", path = "/api/employee")
public interface EmployeeClient {

    @GetMapping("/{id}")
    EmployeeResponse getEmployeeById(@PathVariable("id") Long id);
}
