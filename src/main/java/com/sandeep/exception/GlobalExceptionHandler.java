package com.sandeep.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleGenricException(DuplicateKeyException e){
        Map<String,Object>response=new HashMap<>();
        response.put("Status", HttpStatus.CONFLICT.value());
        response.put("Message","Email already exists.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

}
