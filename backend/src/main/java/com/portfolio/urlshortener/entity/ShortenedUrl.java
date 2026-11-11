package com.portfolio.urlshortener.entity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="shortened_urls", indexes={@Index(name="idx_short_urls_owner_created",columnList="user_id,created_at"),@Index(name="idx_short_urls_expires",columnList="expires_at")}, uniqueConstraints=@UniqueConstraint(name="uk_short_code",columnNames="short_code"))
public class ShortenedUrl {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private AppUser owner;
 @Column(name="short_code",nullable=false,length=32) private String shortCode;
 @Column(name="original_url",nullable=false,length=2048) private String originalUrl;
 @Column(nullable=false) private boolean enabled=true;
 @Column(name="expires_at") private Instant expiresAt;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
 @Column(name="updated_at",nullable=false) private Instant updatedAt=Instant.now();
 protected ShortenedUrl() {}
 public ShortenedUrl(AppUser owner,String code,String url,Instant expiresAt){this.owner=owner;this.shortCode=code;this.originalUrl=url;this.expiresAt=expiresAt;}
 @PreUpdate void touch(){updatedAt=Instant.now();}
 public boolean isActiveAt(Instant now){return enabled&&(expiresAt==null||expiresAt.isAfter(now));}
 public void setEnabled(boolean enabled){this.enabled=enabled;} public void setOriginalUrl(String url){this.originalUrl=url;}
 public void setExpiresAt(Instant expiresAt){this.expiresAt=expiresAt;}
 public Long getId(){return id;} public AppUser getOwner(){return owner;} public String getShortCode(){return shortCode;} public String getOriginalUrl(){return originalUrl;} public boolean isEnabled(){return enabled;} public Instant getExpiresAt(){return expiresAt;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
