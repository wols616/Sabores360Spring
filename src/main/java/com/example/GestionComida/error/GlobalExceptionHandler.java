// src/main/java/com/example/GestionComida/error/GlobalExceptionHandler.java
package com.example.GestionComida.error;

import com.example.GestionComida.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String,Object>>> handleValidation(MethodArgumentNotValidException ex){
        List<Map<String,Object>> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> {
                    Map<String,Object> m = new LinkedHashMap<String,Object>();
                    m.put("field", e.getField());
                    m.put("message", e.getDefaultMessage());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String,Object> data = new HashMap<String,Object>();
        data.put("details", details);

        return ResponseEntity.badRequest()
                .body(new ApiResponse<Map<String,Object>>(false, data, "validation_failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex){
        // Opcional: logear ex.printStackTrace();
        ex.printStackTrace(); // Para debugging
        System.err.println("ERROR: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("internal_error"));
    }
}
