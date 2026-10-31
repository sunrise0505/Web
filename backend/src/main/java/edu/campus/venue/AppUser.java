package edu.campus.venue;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true, length = 40) public String username;
    @Column(nullable = false, length = 100) public String passwordHash;
    @Column(nullable = false, length = 40) public String displayName;
    @Column(nullable = false, length = 16) public String role;
    protected AppUser() {}
    AppUser(String username, String passwordHash, String displayName, String role) {
        this.username = username; this.passwordHash = passwordHash; this.displayName = displayName; this.role = role;
    }
}
