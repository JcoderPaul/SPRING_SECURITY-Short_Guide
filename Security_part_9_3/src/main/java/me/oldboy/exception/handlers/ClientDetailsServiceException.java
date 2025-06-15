package me.oldboy.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.exception.AccountServiceException;
import me.oldboy.exception.ClientServiceException;
import me.oldboy.exception.DuplicateClientEmailException;
import me.oldboy.exception.EmailNotFoundException;
import me.oldboy.exception.exception_entity.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ClientDetailsServiceException {

    /*
    Это очень грубо, когда мы повесили на обработчик "всех собак", что
    исключения, что их обработчики должны быть специализированными, но
    в данном случае мы изучаем безопасность, а не обработчики исключений.
    */
    @ExceptionHandler({
            AccountServiceException.class,
            ClientServiceException.class,
            DuplicateClientEmailException.class,
            EmailNotFoundException.class
    })
    public ResponseEntity<ExceptionResponse> handleExceptions(RuntimeException exception) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}