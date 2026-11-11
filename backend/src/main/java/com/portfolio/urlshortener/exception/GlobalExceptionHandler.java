package com.portfolio.urlshortener.exception;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(ApiException.class) ResponseEntity<Map<String,Object>> api(ApiException e){return ResponseEntity.status(e.getStatus()).body(Map.of("error",e.getStatus().getReasonPhrase(),"message",e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e){String msg=e.getBindingResult().getFieldErrors().stream().findFirst().map(x->x.getField()+": "+x.getDefaultMessage()).orElse("Invalid request");return ResponseEntity.badRequest().body(Map.of("error","Bad Request","message",msg));}
 @ExceptionHandler(Exception.class) ResponseEntity<Map<String,Object>> other(Exception e){return ResponseEntity.status(500).body(Map.of("error","Internal Server Error","message","An unexpected error occurred"));}
}
