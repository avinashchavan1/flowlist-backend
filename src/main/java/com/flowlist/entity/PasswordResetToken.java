package com.flowlist.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "flowlist_password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email, Instant expiresAt) {
        this.token     = token;
        this.email     = email;
        this.expiresAt = expiresAt;
        this.used      = false;
    }

    public String  getToken()     { return token; }
    public String  getEmail()     { return email; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsed()       { return used; }
    public void    setUsed(boolean used) { this.used = used; }
}
