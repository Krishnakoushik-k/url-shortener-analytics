package com.portfolio.urlshortener.service;
import java.time.Instant;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.urlshortener.entity.ClickEvent;
import com.portfolio.urlshortener.repository.ClickEventRepository;
import com.portfolio.urlshortener.repository.ShortenedUrlRepository;
@Service public class ClickAnalyticsService {private final ClickEventRepository clicks;private final ShortenedUrlRepository urls;public ClickAnalyticsService(ClickEventRepository c,ShortenedUrlRepository u){clicks=c;urls=u;}
 @Async("clickExecutor") @Transactional public void record(Long urlId,String referrer,String ua){urls.findById(urlId).ifPresent(url->clicks.save(new ClickEvent(url,Instant.now(),truncate(referrer,1024),device(ua),browser(ua))));}
 private String truncate(String s,int n){return s==null?null:s.substring(0,Math.min(s.length(),n));}private String device(String ua){if(ua==null)return "unknown";String x=ua.toLowerCase();if(x.contains("mobile")||x.contains("android")||x.contains("iphone"))return "mobile";if(x.contains("ipad")||x.contains("tablet"))return "tablet";return "desktop";}private String browser(String ua){if(ua==null)return "unknown";if(ua.contains("Edg/"))return "Edge";if(ua.contains("Firefox/"))return "Firefox";if(ua.contains("Chrome/"))return "Chrome";if(ua.contains("Safari/"))return "Safari";return "other";}
}
