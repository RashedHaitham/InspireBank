package com.example.SecurityService.service;

import com.example.SecurityService.model.User;
import com.example.SecurityService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

class UserServiceTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUsername_shouldReturnUserIfFound() {
        // Arrange
        String username = "rashed";
        User user = new User();
        user.setUsername(username);
        user.setFullName("rashed alqatarneh");

        when(ldapTemplate.findOne(any(), eq(User.class))).thenReturn(user);

        // Act
        Mono<User> result = userService.findByUsername(username);

        // Assert
        User foundUser = result.block();
        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
        verify(ldapTemplate, times(1)).findOne(any(), eq(User.class));
    }

    @Test
    void findByUsername_shouldReturnEmptyIfUserNotFound() {
        String username = "unknown";

        when(ldapTemplate.findOne(any(), eq(User.class))).thenReturn(null);

        Mono<User> result = userService.findByUsername(username);

        assertEquals(Boolean.FALSE, result.hasElement().block());
        verify(ldapTemplate, times(1)).findOne(any(), eq(User.class));
    }


    @Test
    void deletePerson_shouldDeleteUserSuccessfully() {
        // Arrange
        User user = new User();
        user.setUsername("rashed");
        user.setFullName("rashed alqatarneh");

        doNothing().when(userRepository).delete(user);

        // Act
        Mono<Void> result = userService.deletePerson(user);

        // Assert
        result.block();
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deletePerson_shouldThrowExceptionOnError() {
        // Arrange
        User user = new User();
        user.setUsername("rashed");
        user.setFullName("rashed alqatarneh");

        doThrow(new RuntimeException("Delete failed")).when(userRepository).delete(user);

        // Act & Assert
        Mono<Void> result = userService.deletePerson(user);
        RuntimeException exception = assertThrows(RuntimeException.class, result::block);
        assertEquals("Error deleting person: rashed alqatarneh", exception.getMessage());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setUsername("rashed");
        user1.setFullName("rashed alqatarneh");
        user1.setId(LdapNameBuilder.newInstance().add("ou", "admins").build());

        User user2 = new User();
        user2.setUsername("ahmad");
        user2.setFullName("ahmad qaqa");
        user2.setId(LdapNameBuilder.newInstance().add("ou", "users").build());

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        Flux<User> result = userService.findAll();

        // Assert
        List<User> users = result.collectList().block();
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("admin", users.get(0).getRole());
        assertEquals("user", users.get(1).getRole());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void savePerson_shouldSaveUserSuccessfully() {
        // Arrange
        User user = new User();
        user.setUsername("rashed");
        user.setFullName("rashed alqatarneh");
        user.setLastName("alqatarneh");
        user.setEmail("rashed.q@test.com");
        user.setPassword("password");
        user.setRole("admin");

        Name dn = LdapNameBuilder.newInstance()
                .add("ou", "admins")
                .add("uid", user.getUsername())
                .build();

        doThrow(new NameNotFoundException("no name")).when(ldapTemplate).lookup(dn);
        doNothing().when(ldapTemplate).bind(any(DirContextAdapter.class));

        Mono<User> result = userService.savePerson(user);

        User savedUser = result.block();
        assertNotNull(savedUser);
        assertEquals("rashed", savedUser.getUsername());
        verify(ldapTemplate, times(1)).lookup(dn);
        verify(ldapTemplate, times(1)).bind(any(DirContextAdapter.class));
    }

    @Test
    void savePerson_shouldThrowExceptionIfUserAlreadyExists() {
        // Arrange
        User user = new User();
        user.setUsername("rashed");
        user.setRole("admin");

        Name dn = LdapNameBuilder.newInstance()
                .add("ou", "admins")
                .add("uid", user.getUsername())
                .build();

        when(ldapTemplate.lookup(dn)).thenReturn(new DirContextAdapter());

        // Act & Assert
        Mono<User> result = userService.savePerson(user);
        IllegalStateException exception = assertThrows(IllegalStateException.class, result::block);
        assertEquals("Entry already exists: " + dn.toString(), exception.getMessage());
        verify(ldapTemplate, times(1)).lookup(dn);
        verify(ldapTemplate, never()).bind(any(DirContextAdapter.class));
    }
}
