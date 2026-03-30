package com.flowlist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @Email(message = "Valid email required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public void setEmail(String v)    { this.email = v; }
    public void setPassword(String v) { this.password = v; }
}
