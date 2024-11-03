package br.com.washington.cloudflare_service.controllers;

import br.com.washington.cloudflare_service.dto.UploadDTO;
import br.com.washington.cloudflare_service.services.CloudService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CloudController {

    private final CloudService service;

    public CloudController(CloudService service) {
        this.service = service;
    }

    @GetMapping(value = "/req-url-to-upload")
    public ResponseEntity<UploadDTO> reqUrlToUpload(@RequestParam(value = "type") String type){
        return service.reqUrlToUpload(type);
    }

    @GetMapping("/req-url-to-download/{code}")
    public ResponseEntity<UploadDTO> reqUrlToDownload(@PathVariable(value = "code") @NotNull String code){
        return service.reqUrlToDownload(code);
    }

    @PostMapping("/file-stored/{code}")
    public ResponseEntity<Void> fileStored(@PathVariable(value = "code") @NotNull String code){
        return service.fileStoredInCloud(code);
    }
}
