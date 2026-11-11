package com.portfolio.urlshortener.service;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import java.util.*;
class ShortCodeGeneratorTest {@Test void generatesSevenUrlSafeCharacters(){var g=new ShortCodeGenerator();String c=g.nextCode();assertEquals(7,c.length());assertTrue(c.matches("[A-Za-z0-9]{7}"));}@Test void generatesDifferentValuesAcrossSample(){var g=new ShortCodeGenerator();Set<String>s=new HashSet<>();for(int i=0;i<1000;i++)s.add(g.nextCode());assertTrue(s.size()>990);}}
