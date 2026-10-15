
package com.parkinglima.users.service;

import com.parkinglima.users.domain.User;
import com.parkinglima.users.dto.CreateUserRequest;
import com.parkinglima.users.dto.UserResponse;
import com.parkinglima.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    private final EntityManager entityManager;

    public UserService(UserRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return repository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        String fullName = request.fullName().trim();

        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already registered");
        }

        User saved = repository.saveAndFlush(new User(fullName, email));
        entityManager.refresh(saved);
        return UserResponse.from(saved);

    }
}
