package br.com.washington.cloudflare_service.services;

import br.com.washington.cloudflare_service.dto.DeleteMessageDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.time.Instant;
import java.util.logging.Logger;

@Service
public class DeleteService {

    private final S3Client s3Client;

    private final Logger logger = Logger.getLogger(DeleteService.class.getName());

    @Value("${env-variable.cloudflare.bucket-name}")
    private String CLOUDFLARE_BUCKET_NAME = "";

    @Value("${env-variable.delete.delay}")
    private long delay;

    public DeleteService(RabbitTemplate rabbitTemplate, S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @RabbitListener(queues = "${env-variable.rabbitmq.queue-delete}")
    public void callDeleteQueue(DeleteMessageDTO delete){
        logger.info("listening queue - file: " + delete.getCode());
        this.deleteFile(delete.getCode());
        logger.info("Queue listened");
    }

    @Scheduled(fixedRate = 3600000)
    public void callDeleteScheduled(){
        // logger.info("starting scheduled");
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(CLOUDFLARE_BUCKET_NAME)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
        for(int i = 0; i < listObjectsV2Response.keyCount(); i++){
            if(Instant.now().isAfter(listObjectsV2Response.contents().get(i).lastModified().plusMillis(delay))){
              this.deleteFile(listObjectsV2Response.contents().get(i).key());
            }
        }
        logger.info("Scheduled finalized");
    }

    private void deleteFile(String code){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(CLOUDFLARE_BUCKET_NAME)
                .key(code)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        logger.info("deleting: " + code);
    }

}
