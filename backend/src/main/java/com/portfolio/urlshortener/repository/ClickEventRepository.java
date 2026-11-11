package com.portfolio.urlshortener.repository;
import com.portfolio.urlshortener.entity.ClickEvent; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.Instant; import java.util.*;
public interface ClickEventRepository extends JpaRepository<ClickEvent,Long>{
 long countByShortenedUrlId(Long id);
 @Query("select count(c) from ClickEvent c where c.shortenedUrl.owner.id=:ownerId") long countForOwner(@Param("ownerId") Long ownerId);
 @Query("select count(c) from ClickEvent c where c.shortenedUrl.id=:id and c.clickedAt>=:start and c.clickedAt<:end") long countBetween(@Param("id") Long id,@Param("start") Instant start,@Param("end") Instant end);
 @Query("select c from ClickEvent c where c.shortenedUrl.id=:id order by c.clickedAt desc") List<ClickEvent> recent(@Param("id") Long id,org.springframework.data.domain.Pageable pageable);
 @Query("select function('date',c.clickedAt),count(c) from ClickEvent c where c.shortenedUrl.id=:id and c.clickedAt>=:start group by function('date',c.clickedAt) order by function('date',c.clickedAt)") List<Object[]> daily(@Param("id") Long id,@Param("start") Instant start);
}
