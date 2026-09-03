package ru.monyamau.cloudfilestorage.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.monyamau.cloudfilestorage.dto.response.ErrorDto;
import ru.monyamau.cloudfilestorage.exception.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidException(MethodArgumentNotValidException e) {
        ErrorDto errorDto = new ErrorDto("Any validation error");
        if (e.getBindingResult().getFieldError() != null) {
            errorDto = new ErrorDto(e.getBindingResult().getFieldError().getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorDto> handleValidException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDto> handleAuthException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler({ResourceAlreadyExistsException.class, UserAlreadyExistsException.class})
    public ResponseEntity<ErrorDto> handleConflictException(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleAnyException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(e.getMessage()));
    }
}