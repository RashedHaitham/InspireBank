package com.example.employeeService;

import com.example.employeeService.dto.EmployeeCreationRequest;
import com.example.employeeService.model.Employee;
import com.example.employeeService.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeServiceApplicationTests {

	@LocalServerPort
	private int port;

	private String baseUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private EmployeeRepository employeeRepository;

	EmployeeCreationRequest request;

	@BeforeEach
	public void setUp() {
		request = new EmployeeCreationRequest();
		request.setEmail("example@gmail.com");
		request.setName("Rashed Qatarneh");
		request.setPosition("Java Developer");

		baseUrl = "http://localhost:" + port + "/api/employee";

		employeeRepository.deleteAll();
	}

	@Test
	public void testCreateEmployee() {
		HttpEntity<EmployeeCreationRequest> requestEntity = new HttpEntity<>(request);

		ResponseEntity<Employee> responseEntity = restTemplate.postForEntity(baseUrl, requestEntity, Employee.class);

		assertEquals(201, responseEntity.getStatusCodeValue());
		Employee createdEmployee = responseEntity.getBody();
		assertNotNull(createdEmployee);
		assertEquals(request.getName(), createdEmployee.getName());
		assertEquals(request.getEmail(), createdEmployee.getEmail());
		assertEquals(request.getPosition(), createdEmployee.getPosition());
	}

	@Test
	public void testGetEmployeeById() {
		HttpEntity<EmployeeCreationRequest> requestEntity = new HttpEntity<>(request);
		ResponseEntity<Employee> postResponse = restTemplate.postForEntity(baseUrl, requestEntity, Employee.class);
		Employee createdEmployee = postResponse.getBody();

		ResponseEntity<Employee> responseEntity = restTemplate.getForEntity(baseUrl + "/" + createdEmployee.getId(), Employee.class);

		assertEquals(200, responseEntity.getStatusCodeValue());
		Employee fetchedEmployee = responseEntity.getBody();
		assertNotNull(fetchedEmployee);
		assertEquals(createdEmployee.getId(), fetchedEmployee.getId());
		assertEquals(createdEmployee.getName(), fetchedEmployee.getName());
		assertEquals(createdEmployee.getEmail(), fetchedEmployee.getEmail());
	}

	@Test
	public void testUpdateEmployee() {
		HttpEntity<EmployeeCreationRequest> requestEntity = new HttpEntity<>(request);
		ResponseEntity<Employee> postResponse = restTemplate.postForEntity(baseUrl, requestEntity, Employee.class);
		Employee createdEmployee = postResponse.getBody();

		EmployeeCreationRequest updatedRequest = new EmployeeCreationRequest();
		updatedRequest.setName("Updated Name");
		updatedRequest.setEmail("updated.email@example.com");
		updatedRequest.setPosition("Updated Position");

		HttpEntity<EmployeeCreationRequest> updatedRequestEntity = new HttpEntity<>(updatedRequest);
		ResponseEntity<Employee> updateResponse = restTemplate.exchange(baseUrl + "/" + createdEmployee.getId(), HttpMethod.PUT, updatedRequestEntity, Employee.class);

		assertEquals(200, updateResponse.getStatusCodeValue());
		Employee updatedEmployee = updateResponse.getBody();
		assertNotNull(updatedEmployee);
		assertEquals(updatedRequest.getName(), updatedEmployee.getName());
		assertEquals(updatedRequest.getEmail(), updatedEmployee.getEmail());
		assertEquals(updatedRequest.getPosition(), updatedEmployee.getPosition());
	}

	@Test
	public void testDeleteEmployee() {
		HttpEntity<EmployeeCreationRequest> requestEntity = new HttpEntity<>(request);
		ResponseEntity<Employee> postResponse = restTemplate.postForEntity(baseUrl, requestEntity, Employee.class);
		Employee createdEmployee = postResponse.getBody();

		restTemplate.delete(baseUrl + "/" + createdEmployee.getId());

		ResponseEntity<Employee> responseEntity = restTemplate.getForEntity(baseUrl + "/" + createdEmployee.getId(), Employee.class);

		assertEquals(200, responseEntity.getStatusCodeValue());
	}
}
