package me.oldboy.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.exception.EmailNotFoundException;
import me.oldboy.exception.exception_entity.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ClientDetailsServiceException {

    @ExceptionHandler({UsernameNotFoundException.class,
                       EmailNotFoundException.class,
                       AuthenticationException.class,
                       DuplicateClientEmailException.class
    })
    public ResponseEntity<ExceptionResponse> handleExceptions(RuntimeException exception) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}