
package com.portfolio.urlshortener.controller;

import java.net.URI;
import java.time.Duration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.urlshortener.dto.UrlDtos.AnalyticsResponse;
import com.portfolio.urlshortener.dto.UrlDtos.CreateUrlRequest;
import com.portfolio.urlshortener.dto.UrlDtos.DashboardResponse;
import com.portfolio.urlshortener.dto.UrlDtos.UpdateUrlRequest;
import com.portfolio.urlshortener.dto.UrlDtos.UrlResponse;
import com.portfolio.urlshortener.service.ClickAnalyticsService;
import com.portfolio.urlshortener.service.RateLimitService;
import com.portfolio.urlshortener.service.UrlService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class UrlController {

    private final UrlService service;
    private final RateLimitService limits;
    private final ClickAnalyticsService analytics;

    public UrlController(
            UrlService s,
            RateLimitService l,
            ClickAnalyticsService a
    ) {
        service = s;
        limits = l;
        analytics = a;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> create(
            @RequestBody @Valid CreateUrlRequest r,
            java.security.Principal p,
            HttpServletRequest req
    ) {
        String client = clientIp(req);

        if (!limits.allow("create:" + client, 20, Duration.ofHours(1))) {
            return ResponseEntity.status(429).build();
        }

        return ResponseEntity.status(201)
                .body(service.create(p.getName(), r));
    }

    @GetMapping("/api/urls/{id}")
    public UrlResponse get(
            @PathVariable Long id,
            java.security.Principal p
    ) {
        return service.get(p.getName(), id);
    }

    @GetMapping("/api/urls")
    public Page<UrlResponse> list(
            java.security.Principal p,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }

        String[] s = sort.split(",");

        Sort order = Sort.by(
                s.length > 1 && s[1].equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                s[0]
        );

        return service.list(
                p.getName(),
                enabled,
                PageRequest.of(Math.max(0, page), size, order)
        );
    }

    @PatchMapping("/api/urls/{id}")
    public UrlResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUrlRequest r,
            java.security.Principal p
    ) {
        return service.update(p.getName(), id, r);
    }

    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            java.security.Principal p
    ) {
        service.delete(p.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/urls/{id}/analytics")
    public AnalyticsResponse analytics(
            @PathVariable Long id,
            java.security.Principal p
    ) {
        return service.analytics(p.getName(), id);
    }

    @GetMapping("/api/dashboard")
    public DashboardResponse dashboard(java.security.Principal p) {
        return service.dashboard(p.getName());
    }

    @GetMapping("/r/{code}")
    @SecurityRequirement(name = "")
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            HttpServletRequest req
    ) {
        if (!limits.allow(
                "redirect:" + clientIp(req),
                300,
                Duration.ofMinutes(1)
        )) {
            return ResponseEntity.status(429).build();
        }

        var url = service.resolve(code);

        try {
            analytics.record(
                    url.getId(),
                    req.getHeader("Referer"),
                    req.getHeader("User-Agent")
            );
        } catch (RuntimeException ignored) {
            // Never fail a redirect because analytics is unavailable
            // or its queue is full.
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }

    private String clientIp(HttpServletRequest r) {
        // Trust X-Forwarded-For only when a trusted reverse proxy sanitizes it.
        return r.getRemoteAddr();
    }
}
