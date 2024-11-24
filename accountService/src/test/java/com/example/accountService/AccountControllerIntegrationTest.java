package com.example.accountService;

import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.model.Account;
import com.example.accountService.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AccountServiceApplication.class)
@ActiveProfiles("test") // Load application-test.yml
@AutoConfigureMockMvc
public class AccountControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AccountRepository accountRepository;

	@BeforeEach
	void setUp() {
		accountRepository.deleteAll();
	}

	@Test
	void testCreateAccount() throws Exception {
		mockMvc.perform(post("/api/account")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                    {
                        "accountNumber": "1234567890",
                        "employeeId": 42,
                        "initialBalance": 5000.0
                    }
                    """))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accountNumber", is("1234567890")))
				.andExpect(jsonPath("$.balance", is(5000.0)));

		Optional<Account> savedAccount = accountRepository.findByAccountNumber("1234567890");
		assert (savedAccount.isPresent());
	}


	@Test
	void testGetAccountByNumber() throws Exception {
		Account account = new Account();
		account.setAccountNumber("12345678950");
		account.setBalance(1000.0);
		accountRepository.save(account);

		mockMvc.perform(get("/api/account/12345678950"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber", is("12345678950")))
				.andExpect(jsonPath("$.balance", is(1000.0)));
	}

	@Test
	void testUpdateAccount() throws Exception {
		// Arrange: Save the initial account
		Account account = new Account();
		account.setAccountNumber("1234567890");
		account.setBalance(1000.0);
		account.setEmployeeId(42L);
		accountRepository.save(account);

		// Act: Perform the update request
		mockMvc.perform(put("/api/account/1234567890")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                    {
                        "accountNumber": "1234567890",
                        "employeeId": 42,
                        "initialBalance": 2000.0
                    }
                    """))
				// Assert: Verify the response status and fields
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber", is("1234567890")))
				.andExpect(jsonPath("$.balance", is(2000.0)))
				.andExpect(jsonPath("$.employeeId", is(42)));

		// Assert: Verify the account is updated in the database
		Optional<Account> updatedAccount = accountRepository.findByAccountNumber("1234567890");
		assert (updatedAccount.isPresent());
		assert (updatedAccount.get().getBalance().equals(2000.0));
	}


	@Test
	void testDeleteAccount() throws Exception {
		Account account = new Account();
		account.setAccountNumber("123456789");
		account.setBalance(1000.0);
		accountRepository.save(account);

		mockMvc.perform(delete("/api/account/123456789"))
				.andExpect(status().isNoContent());

		Optional<Account> deletedAccount = accountRepository.findByAccountNumber("123456789");
		assert (deletedAccount.isEmpty());
	}

	@Test
	void testGetAllAccounts() throws Exception {
		Account account1 = new Account();
		account1.setAccountNumber("1234567891");
		account1.setBalance(1000.0);

		Account account2 = new Account();
		account2.setAccountNumber("1234567890");
		account2.setBalance(2000.0);

		accountRepository.save(account1);
		accountRepository.save(account2);

		mockMvc.perform(get("/api/account/all?page=0&size=2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[0].accountNumber", is("1234567890")))
				.andExpect(jsonPath("$.content[1].accountNumber", is("1234567891")));
	}
}
