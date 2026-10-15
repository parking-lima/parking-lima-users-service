
package com.parkinglima.users.api;

import com.parkinglima.users.dto.UserResponse;
import com.parkinglima.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import static org.hamcrest.Matchers.is;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @Test
  void shouldListUsers() throws Exception {
    when(userService.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/users"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  void shouldReturn404WhenUserDoesNotExist() throws Exception {
    when(userService.findById(999L))
        .thenThrow(new ResponseStatusException(
            HttpStatus.NOT_FOUND, "User not found"));

    mockMvc.perform(get("/api/v1/users/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldCreateUser() throws Exception {
    UserResponse response = new UserResponse(
        1L,
        "Usuario de prueba",
        "prueba@example.com",
        OffsetDateTime.parse("2026-10-15T08:00:00Z"));

    when(userService.create(any())).thenReturn(response);

    mockMvc.perform(post("/api/v1/users")
        .contentType("application/json")
        .content("""
            {
              "fullName": "Usuario de prueba",
              "email": "prueba@example.com"
            }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email")
            .value("prueba@example.com"));
  }

  @Test
  void shouldRejectInvalidEmail() throws Exception {
    mockMvc.perform(post("/api/v1/users")
        .contentType("application/json")
        .content("""
            {
              "fullName": "Usuario de prueba",
              "email": "correo-invalido"
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectBlankName() throws Exception {
    mockMvc.perform(post("/api/v1/users")
        .contentType("application/json")
        .content("""
            {
              "fullName": "",
              "email": "nuevo@example.com"
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409ForDuplicateEmail() throws Exception {
    when(userService.create(any()))
        .thenThrow(new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Email already registered"));

    mockMvc.perform(post("/api/v1/users")
        .contentType("application/json")
        .content("""
            {
              "fullName": "Usuario duplicado",
              "email": "prueba@example.com"
            }
            """))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldReturn409WhenDatabaseRejectsDuplicate() throws Exception {
    when(userService.create(any()))
        .thenThrow(new DataIntegrityViolationException(
            "Duplicate key constraint violation"));

    mockMvc.perform(post("/api/v1/users")
        .contentType("application/json")
        .content("""
            {
              "fullName": "Usuario duplicado",
              "email": "prueba@example.com"
            }
            """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error",
            is("Data integrity violation")));
  }

}
