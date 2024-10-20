package com.example.accountService;

import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.model.Account;
import com.example.accountService.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountServiceApplicationTests {

	@LocalServerPort
	private int port;

	private String baseUrl;

	@Autowired
	private RestTemplate restTemplate;


	@Autowired
	private AccountRepository accountRepository;

	AccountCreationRequest request;

	@BeforeEach
	public void setUp() {
		// Setup request object for tests
		request = new AccountCreationRequest();
		request.setAccountNumber("1234567890A");
		request.setInitialBalance(1000.0);
		request.setEmployeeId(1L);

		baseUrl = "http://localhost:" + port + "/api/account";

		// Clear the repository before each test
		accountRepository.deleteAll();
	}

	@Test
	void testFindAccountById() {
		// Insert test data using the repository
		Account account = new Account();
		account.setAccountNumber("1234567890A");
		account.setBalance(1000.0);
		account.setEmployeeId(1L);
		accountRepository.save(account);

		Account resultAccount = restTemplate.getForObject(baseUrl + "/{id}", Account.class, "1234567890A");
		Assertions.assertNotNull(resultAccount);
		Assertions.assertEquals(1000.0, resultAccount.getBalance());
		Assertions.assertEquals(1, accountRepository.findAll().size());
	}

	@Test
	void testAddAccount() {
		AccountCreationRequest request = new AccountCreationRequest();
		request.setAccountNumber("1234567890A");
		request.setInitialBalance(1000.0);
		request.setEmployeeId(36L);

		Account response = restTemplate.postForObject(baseUrl, request, Account.class);

		Assertions.assertNotNull(response);
		Assertions.assertEquals(request.getAccountNumber(), response.getAccountNumber());
		Assertions.assertEquals(1, accountRepository.findAll().size());
	}

	@Test
	void testDeleteAccount() {
		// Insert test data
		Account account = new Account();
		account.setAccountNumber("1234567890A");
		account.setBalance(1000.0);
		account.setEmployeeId(1L);
		accountRepository.save(account);

		restTemplate.delete(baseUrl + "/{id}", "1234567890A");
		Assertions.assertEquals(0, accountRepository.findAll().size());
	}

	@Test
	void testFindAllAccounts() {
		// Insert test data
		Account account = new Account();
		account.setAccountNumber("1234567890A");
		account.setBalance(1000.0);
		account.setEmployeeId(1L);
		accountRepository.save(account);

		List<Account> accounts = restTemplate.getForObject(baseUrl + "/all", List.class);
		Assertions.assertNotNull(accounts);
		Assertions.assertEquals(1, accounts.size());
		Assertions.assertEquals(1, accountRepository.findAll().size());
	}
}
