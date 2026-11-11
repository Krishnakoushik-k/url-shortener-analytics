package com.portfolio.urlshortener.service;
import org.springframework.stereotype.Component; import java.security.SecureRandom;
@Component public class ShortCodeGenerator {private static final char[] ALPHABET="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();private final SecureRandom random=new SecureRandom();public String nextCode(){char[] out=new char[7];for(int i=0;i<out.length;i++)out[i]=ALPHABET[random.nextInt(ALPHABET.length)];return new String(out);}}
