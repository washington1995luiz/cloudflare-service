package br.com.washington.cloudflare_service.exception;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${env-variable.local-zone-id}")
    private String LOCAL_ZONE_ID;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handlerAllException(Exception ex, WebRequest request){
        return response(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileNameNotFoundInRedisException.class)
    public ResponseEntity<ExceptionResponse> handlerFileNameNotFoundInRedisException(Exception ex, WebRequest request){
        return response(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileNotExistsInCloudException.class)
    public ResponseEntity<ExceptionResponse> handlerFileNotExistsInCloudException(Exception ex, WebRequest request){
        return response(ex, request, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ExceptionResponse> response(Exception ex, WebRequest request, HttpStatus httpStatus){
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of(LOCAL_ZONE_ID));
        return new ResponseEntity<>(
                new ExceptionResponse(
                        ex.getMessage(),
                        request.getDescription(false),
                        time.toString()
                ),
                httpStatus
        );
    }
}
