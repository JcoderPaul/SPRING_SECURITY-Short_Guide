package me.oldboy.exception.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class DtoValidationHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException exception) throws JsonProcessingException {

        Map<String, String> mapValidationErrors = new HashMap<>();
        exception.getBindingResult()
                .getAllErrors()
                .forEach(error -> {String fieldName = ((FieldError) error).getField();
                                                       String errorMessage = error.getDefaultMessage();
                                                       mapValidationErrors.put(fieldName, errorMessage);
                });

        return ResponseEntity.badRequest()
                             .body(new ObjectMapper().writer()
                                                     .withDefaultPrettyPrinter()
                                                     .writeValueAsString(mapValidationErrors));
    }
}
