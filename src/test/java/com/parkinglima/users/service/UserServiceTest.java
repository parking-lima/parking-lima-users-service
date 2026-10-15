
package com.parkinglima.users.service;

import com.parkinglima.users.domain.User;
import com.parkinglima.users.dto.CreateUserRequest;
import com.parkinglima.users.dto.UserResponse;
import com.parkinglima.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository repository;

  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private UserService service;

  @Test
  void shouldReturnAllUsers() {
    User user = new User(
        "Usuario de prueba",
        "prueba@example.com");

    when(repository.findAll()).thenReturn(List.of(user));

    var result = service.findAll();

    assertEquals(1, result.size());
    assertEquals("Usuario de prueba", result.get(0).fullName());
    assertEquals("prueba@example.com", result.get(0).email());

    verify(repository).findAll();
  }

  @Test
  void shouldReturnNotFoundWhenUserDoesNotExist() {
    when(repository.findById(999L))
        .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> service.findById(999L));

    assertEquals(404, exception.getStatusCode().value());
    verify(repository).findById(999L);
  }

  @Test
  void shouldCreateUserSuccessfully() {
    CreateUserRequest request = new CreateUserRequest(
        "Usuario de prueba",
        "PRUEBA@example.com");

    User savedUser = new User(
        "Usuario de prueba",
        "prueba@example.com");

    when(repository.existsByEmail("prueba@example.com"))
        .thenReturn(false);

    when(repository.saveAndFlush(any(User.class)))
        .thenReturn(savedUser);

    UserResponse response = service.create(request);

    assertEquals("Usuario de prueba", response.fullName());
    assertEquals("prueba@example.com", response.email());

    verify(repository).saveAndFlush(any(User.class));
    verify(entityManager).refresh(savedUser);
  }

  @Test
  void shouldRejectDuplicateEmail() {
    CreateUserRequest request = new CreateUserRequest(
        "Usuario duplicado",
        "PRUEBA@example.com");

    when(repository.existsByEmail("prueba@example.com"))
        .thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> service.create(request));

    assertEquals(409, exception.getStatusCode().value());

    verify(repository, never()).saveAndFlush(any(User.class));
    verifyNoInteractions(entityManager);
  }

  @Test
  void shouldNormalizeEmailBeforeCheckingDuplicates() {
    CreateUserRequest request = new CreateUserRequest(
        "  Usuario de prueba  ",
        "  PRUEBA@EXAMPLE.COM  ");

    when(repository.existsByEmail("prueba@example.com"))
        .thenReturn(true);

    assertThrows(
        ResponseStatusException.class,
        () -> service.create(request));

    verify(repository).existsByEmail("prueba@example.com");
  }

}
