package com.portfolio.urlshortener.entity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="users", uniqueConstraints=@UniqueConstraint(name="uk_users_email", columnNames="email"))
public class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=190) private String email;
 @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
 @Column(nullable=false,length=40) private String role="USER";
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
 protected AppUser() {}
 public AppUser(String email,String passwordHash){this.email=email;this.passwordHash=passwordHash;}
 public Long getId(){return id;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public String getRole(){return role;} public Instant getCreatedAt(){return createdAt;}
}
