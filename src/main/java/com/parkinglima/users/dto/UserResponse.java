
package com.parkinglima.users.dto;

import com.parkinglima.users.domain.User;
import java.time.OffsetDateTime;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    OffsetDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}
