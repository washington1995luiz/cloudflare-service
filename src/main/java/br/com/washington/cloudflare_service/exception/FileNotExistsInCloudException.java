package br.com.washington.cloudflare_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotExistsInCloudException extends RuntimeException {
    public FileNotExistsInCloudException(String message) {
        super(message);
    }
}
