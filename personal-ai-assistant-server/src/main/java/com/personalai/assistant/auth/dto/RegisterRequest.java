package com.personalai.assistant.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 64) String username,
    @NotBlank @Size(min = 6) String password
) {}
