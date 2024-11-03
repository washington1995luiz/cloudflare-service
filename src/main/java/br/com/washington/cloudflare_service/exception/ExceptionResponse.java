package br.com.washington.cloudflare_service.exception;

import java.io.Serial;
import java.io.Serializable;

public class ExceptionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String message;
    private String description;
    private String timestamp;

    public ExceptionResponse() {
    }

    public ExceptionResponse(String message, String description, String timestamp) {
        this.message = message;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
