package com.portfolio.urlshortener.repository;
import com.portfolio.urlshortener.entity.ShortenedUrl; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl,Long>{
 Optional<ShortenedUrl> findByShortCode(String shortCode); boolean existsByShortCode(String shortCode);
 Optional<ShortenedUrl> findByIdAndOwnerId(Long id,Long ownerId);
 Page<ShortenedUrl> findByOwnerId(Long ownerId,Pageable pageable);
 @Query("select s from ShortenedUrl s where s.owner.id=:ownerId and (:enabled is null or s.enabled=:enabled)") Page<ShortenedUrl> findOwned(@Param("ownerId") Long ownerId,@Param("enabled") Boolean enabled,Pageable pageable);
 @Query("select count(s) from ShortenedUrl s where s.owner.id=:ownerId") long countForOwner(@Param("ownerId") Long ownerId);
}
