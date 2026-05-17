package com.flowlist.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(length = 255)
    private String name;

    @Column(name = "google_id", length = 255)
    private String googleId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public User() {}

    public Long getId()           { return id; }
    public String getEmail()      { return email; }
    public String getPassword()   { return password; }
    public String getName()       { return name; }
    public Instant getCreatedAt() { return createdAt; }

    public String getGoogleId()                  { return googleId; }
    public void setId(Long id)               { this.id = id; }
    public void setEmail(String email)       { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name)         { this.name = name; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public void setCreatedAt(Instant t)      { this.createdAt = t; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final User u = new User();
        public Builder name(String v)       { u.name = v;       return this; }
        public Builder email(String v)      { u.email = v;      return this; }
        public Builder password(String v)   { u.password = v;   return this; }
        public Builder googleId(String v)   { u.googleId = v;   return this; }
        public User build() { return u; }
    }
}
