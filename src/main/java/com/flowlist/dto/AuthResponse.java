package com.flowlist.dto;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresInDays;
    private UserDto user;

    public String getToken()         { return token; }
    public String getTokenType()     { return tokenType; }
    public long getExpiresInDays()   { return expiresInDays; }
    public UserDto getUser()         { return user; }

    public void setToken(String v)          { this.token = v; }
    public void setTokenType(String v)      { this.tokenType = v; }
    public void setExpiresInDays(long v)    { this.expiresInDays = v; }
    public void setUser(UserDto v)          { this.user = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AuthResponse r = new AuthResponse();
        public Builder token(String v)          { r.token = v;          return this; }
        public Builder tokenType(String v)      { r.tokenType = v;      return this; }
        public Builder expiresInDays(long v)    { r.expiresInDays = v;  return this; }
        public Builder user(UserDto v)          { r.user = v;           return this; }
        public AuthResponse build()             { return r; }
    }

    public static class UserDto {
        private Long id;
        private String name;
        private String email;

        public Long getId()      { return id; }
        public String getName()  { return name; }
        public String getEmail() { return email; }
        public void setId(Long v)      { this.id = v; }
        public void setName(String v)  { this.name = v; }
        public void setEmail(String v) { this.email = v; }

        public static UBuilder builder() { return new UBuilder(); }

        public static class UBuilder {
            private final UserDto u = new UserDto();
            public UBuilder id(Long v)      { u.id = v;    return this; }
            public UBuilder name(String v)  { u.name = v;  return this; }
            public UBuilder email(String v) { u.email = v; return this; }
            public UserDto build()          { return u; }
        }
    }
}
