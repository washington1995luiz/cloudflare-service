package br.com.washington.cloudflare_service.dto;

public class DeleteMessageDTO {

    private String code;

    public DeleteMessageDTO() {
    }

    public DeleteMessageDTO(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
