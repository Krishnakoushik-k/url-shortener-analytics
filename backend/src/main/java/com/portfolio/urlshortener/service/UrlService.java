package com.portfolio.urlshortener.service;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.urlshortener.dto.UrlDtos.AnalyticsResponse;
import com.portfolio.urlshortener.dto.UrlDtos.CreateUrlRequest;
import com.portfolio.urlshortener.dto.UrlDtos.DailyCount;
import com.portfolio.urlshortener.dto.UrlDtos.DashboardResponse;
import com.portfolio.urlshortener.dto.UrlDtos.RecentClick;
import com.portfolio.urlshortener.dto.UrlDtos.UpdateUrlRequest;
import com.portfolio.urlshortener.dto.UrlDtos.UrlResponse;
import com.portfolio.urlshortener.entity.AppUser;
import com.portfolio.urlshortener.entity.ShortenedUrl;
import com.portfolio.urlshortener.exception.ApiException;
import com.portfolio.urlshortener.repository.ClickEventRepository;
import com.portfolio.urlshortener.repository.ShortenedUrlRepository;
import com.portfolio.urlshortener.repository.UserRepository;
@Service public class UrlService {private final ShortenedUrlRepository urls;private final UserRepository users;private final ClickEventRepository clicks;private final ShortCodeGenerator codes;private final UrlCacheService cache;private final String baseUrl;
 public UrlService(ShortenedUrlRepository u,UserRepository us,ClickEventRepository c,ShortCodeGenerator g,UrlCacheService cache,org.springframework.core.env.Environment env){urls=u;users=us;clicks=c;codes=g;this.cache=cache;this.baseUrl=env.getProperty("app.base-url","http://localhost:8080");}
 // Intentionally not wrapped in an outer transaction: each repository insert owns its transaction,
 // so a unique-index collision rolls back that insert and the retry can safely continue.
 public UrlResponse create(String email,CreateUrlRequest r){AppUser owner=user(email);String target=validateUrl(r.originalUrl());if(r.expiresAt()!=null&&!r.expiresAt().isAfter(Instant.now()))throw new ApiException(HttpStatus.BAD_REQUEST,"Expiration must be in the future");if(r.customAlias()!=null&&!r.customAlias().isBlank()){String alias=r.customAlias().trim();if(urls.existsByShortCode(alias))throw new ApiException(HttpStatus.CONFLICT,"Alias is already in use");try{return dto(urls.save(new ShortenedUrl(owner,alias,target,r.expiresAt())));}catch(DataIntegrityViolationException e){throw new ApiException(HttpStatus.CONFLICT,"Alias is already in use");}}
 for(int i=0;i<8;i++){String code=codes.nextCode();if(urls.existsByShortCode(code))continue;try{return dto(urls.save(new ShortenedUrl(owner,code,target,r.expiresAt())));}catch(DataIntegrityViolationException e){if(i==7)throw new ApiException(HttpStatus.CONFLICT,"Could not allocate a unique short code; retry");}}
 throw new ApiException(HttpStatus.CONFLICT,"Could not allocate a unique short code; retry");}
 @Transactional(readOnly=true) public UrlResponse get(String email,Long id){return dto(owned(email,id));}
 @Transactional(readOnly=true) public Page<UrlResponse> list(String email,Boolean enabled,Pageable p){return urls.findOwned(user(email).getId(),enabled,p).map(this::dto);}
 @Transactional public UrlResponse update(String email,Long id,UpdateUrlRequest r){ShortenedUrl u=owned(email,id);if(r.originalUrl()!=null)u.setOriginalUrl(validateUrl(r.originalUrl()));if(r.expiresAt()!=null&&!r.expiresAt().isAfter(Instant.now()))throw new ApiException(HttpStatus.BAD_REQUEST,"Expiration must be in the future");if(Boolean.TRUE.equals(r.clearExpiration()))u.setExpiresAt(null);else if(r.expiresAt()!=null)u.setExpiresAt(r.expiresAt());if(r.enabled()!=null)u.setEnabled(r.enabled());cache.evict(u.getShortCode());return dto(urls.save(u));}
 @Transactional public void delete(String email,Long id){ShortenedUrl u=owned(email,id);cache.evict(u.getShortCode());urls.delete(u);}
 @Transactional(readOnly=true) public ShortenedUrl resolve(String code){Optional<String> cached=cache.get(code);if(cached.isPresent()){ShortenedUrl u=urls.findByShortCode(code).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Short link not found"));if(!u.isActiveAt(Instant.now())){cache.evict(code);throw new ApiException(HttpStatus.GONE,"Short link is disabled or expired");}return u;}
 ShortenedUrl u=urls.findByShortCode(code).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Short link not found"));if(!u.isActiveAt(Instant.now()))throw new ApiException(HttpStatus.GONE,"Short link is disabled or expired");Duration ttl=Duration.ofHours(1);if(u.getExpiresAt()!=null){Duration remaining=Duration.between(Instant.now(),u.getExpiresAt());if(remaining.compareTo(ttl)<0)ttl=remaining;}cache.put(code,u.getOriginalUrl(),ttl);return u;}

@Transactional(readOnly = true)
public AnalyticsResponse analytics(String email, Long id) {
    ShortenedUrl u = owned(email, id);

    Instant now = Instant.now();
    Instant thirtyDaysAgo = now.minus(30, java.time.temporal.ChronoUnit.DAYS);

    Instant startOfMonth = LocalDate.now(ZoneOffset.UTC)
            .withDayOfMonth(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();

    List<DailyCount> daily = clicks.daily(id, thirtyDaysAgo)
            .stream()
            .map(x -> new DailyCount(
                    String.valueOf(x[0]),
                    ((Number) x[1]).longValue()
            ))
            .toList();

    List<RecentClick> recent = clicks.recent(id, PageRequest.of(0, 20))
            .stream()
            .map(c -> new RecentClick(
                    c.getClickedAt(),
                    c.getReferrer(),
                    c.getDeviceCategory(),
                    c.getBrowser()
            ))
            .toList();

    long totalClicks = clicks.countByShortenedUrlId(id);

    long clicksLast30Days = clicks.countBetween(
            id, thirtyDaysAgo, now
    );

    long clicksThisMonth = clicks.countBetween(
            id, startOfMonth, now
    );

    return new AnalyticsResponse(
            u.getShortCode(),
            totalClicks,
            clicksLast30Days,
            clicksThisMonth,
            daily,
            recent
    );
}
 @Transactional(readOnly=true) public DashboardResponse dashboard(String email){AppUser u=user(email);Page<ShortenedUrl> page=urls.findByOwnerId(u.getId(),PageRequest.of(0,100,Sort.by(Sort.Direction.DESC,"createdAt")));long totalClicks=clicks.countForOwner(u.getId());return new DashboardResponse(urls.countForOwner(u.getId()),totalClicks,page.map(this::dto).getContent());}
 private AppUser user(String email){return users.findByEmail(email).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"User account not found"));}private ShortenedUrl owned(String email,Long id){return urls.findByIdAndOwnerId(id,user(email).getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"URL not found"));}
 private String validateUrl(String value){try{URI uri=URI.create(value.trim());String scheme=uri.getScheme();if(uri.getHost()==null||!("http".equalsIgnoreCase(scheme)||"https".equalsIgnoreCase(scheme))||uri.getUserInfo()!=null)throw new IllegalArgumentException();return uri.toASCIIString();}catch(Exception e){throw new ApiException(HttpStatus.BAD_REQUEST,"Only valid http and https URLs are allowed");}}
 private UrlResponse dto(ShortenedUrl u){return new UrlResponse(u.getId(),u.getShortCode(),baseUrl+"/r/"+u.getShortCode(),u.getOriginalUrl(),u.isEnabled(),u.getExpiresAt(),u.getCreatedAt());}
}
