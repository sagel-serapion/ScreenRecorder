package com.project.screenrecorder.Exception;


import com.project.screenrecorder.DTO.errorResponse.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage()); // 400
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()); // 500
    }

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVideoNotFoundException(VideoNotFoundException exception){
        return buildError(HttpStatus.NOT_FOUND,exception.getMessage()); // 404
    }

    @ExceptionHandler(VideoNotReadyException.class)
    public ResponseEntity<ErrorResponse> VideoNotReadyException(VideoNotReadyException exception){
        return buildError(HttpStatus.CONFLICT,exception.getMessage()); // 409
    }

    @ExceptionHandler(WrongEndpointException.class)
    public ResponseEntity<ErrorResponse> handleWrongEndpointException(WrongEndpointException exception){
        return buildError(HttpStatus.CONFLICT,exception.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException exception){
        return buildError(HttpStatus.UNAUTHORIZED,exception.getMessage()); // 401
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationErrors(MethodArgumentNotValidException exception){

        Map<String,String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach( error ->
            errors.put(error.getField(),error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorMessage(message);
        return ResponseEntity.status(status).body(response);
    }


}
