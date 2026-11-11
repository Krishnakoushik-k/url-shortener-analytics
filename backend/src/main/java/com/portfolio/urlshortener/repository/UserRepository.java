package com.portfolio.urlshortener.repository;
import com.portfolio.urlshortener.entity.AppUser; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface UserRepository extends JpaRepository<AppUser,Long>{Optional<AppUser> findByEmail(String email); boolean existsByEmail(String email);}
