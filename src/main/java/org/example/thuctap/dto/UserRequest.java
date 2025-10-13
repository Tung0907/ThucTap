package org.example.thuctap.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {
    @NotBlank(message = "Username không được rỗng")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "Password không được rỗng")
    private String password;

    @Size(max = 100)
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String role;

    // getters / setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
