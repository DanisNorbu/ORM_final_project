package com.learnplatform.web.dto;

import com.learnplatform.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserPatchRequest(
        @Size(max = 120) String name,
        @Email @Size(max = 255) String email,
        UserRole role
) {
}
