package com.portfolio.urlshortener.entity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="click_events",indexes={@Index(name="idx_click_url_time",columnList="shortened_url_id,clicked_at"),@Index(name="idx_click_time",columnList="clicked_at")})
public class ClickEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="shortened_url_id",nullable=false) private ShortenedUrl shortenedUrl;
 @Column(name="clicked_at",nullable=false) private Instant clickedAt;
 @Column(length=1024) private String referrer;
 @Column(name="device_category",length=30) private String deviceCategory;
 @Column(length=80) private String browser;
 protected ClickEvent(){}
 public ClickEvent(ShortenedUrl url,Instant at,String referrer,String device,String browser){this.shortenedUrl=url;this.clickedAt=at;this.referrer=referrer;this.deviceCategory=device;this.browser=browser;}
 public Instant getClickedAt(){return clickedAt;} public String getReferrer(){return referrer;} public String getDeviceCategory(){return deviceCategory;} public String getBrowser(){return browser;}
}
