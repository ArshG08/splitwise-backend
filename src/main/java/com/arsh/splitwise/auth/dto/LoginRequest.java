package com.arsh.splitwise.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @Email(message = "invalid email format ")
    @NotBlank(message = "email is required ")
    private String email;

    @NotBlank(message = "password is reqired")
    private String password;
}
