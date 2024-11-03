package br.com.washington.cloudflare_service.services;

import br.com.washington.cloudflare_service.dto.DeleteMessageDTO;
import br.com.washington.cloudflare_service.dto.UploadDTO;
import br.com.washington.cloudflare_service.exception.FileNameNotFoundInRedisException;
import br.com.washington.cloudflare_service.exception.FileNotExistsInCloudException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.checksums.Md5Checksum;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Service
public class CloudService {

    private final S3Client s3Client;
    private final S3Presigner preSigner;
    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final AtomicInteger atomicInteger = new AtomicInteger();

    @Value("${env-variable.cloudflare.bucket-name}")
    private String CLOUDFLARE_BUCKET_NAME = "";

    @Value("${env-variable.redis.key-code-file}")
    private String REDIS_CODE_FILE = "";

    @Value("${env-variable.rabbitmq.topic-exchange}")
    private String RABBITMQ_TOPIC_EXCHANGE = "";

    @Value("${env-variable.rabbitmq.binding-delete}")
    private String RABBITMQ_BINDING_DELETE = "";

    @Value("${env-variable.delete.delay}")
    private long delay;

    private Logger logger = Logger.getLogger(CloudService.class.getName());

    public CloudService(S3Client s3Client, S3Presigner preSigner, RedisTemplate<String, String> redisTemplate, RabbitTemplate rabbitTemplate) {
        this.s3Client = s3Client;
        this.preSigner = preSigner;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    private void createBucketIfNotExists(){
        Set<String> bucketsName = new HashSet<>(s3Client.listBuckets().buckets().stream().map(Bucket::name).toList());
        if(!bucketsName.contains(CLOUDFLARE_BUCKET_NAME)){
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(CLOUDFLARE_BUCKET_NAME)
                    .build();
            s3Client.createBucket(createBucketRequest);
        }
    }

    public ResponseEntity<UploadDTO> reqUrlToUpload(String type) {
        this.createBucketIfNotExists();
        String code = String.valueOf(atomicInteger.incrementAndGet());
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(CLOUDFLARE_BUCKET_NAME)
                .key(code + "." + type.split("/")[1])
                .contentType(type)
                .contentDisposition("inline; filename=\"" + code + "." + type.split("/")[1] + "\"")
                .build();

        PutObjectPresignRequest preSignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMillis(delay))
                .build();
        String url = preSigner.presignPutObject(preSignRequest).url().toExternalForm();
        return ResponseEntity.ok(new UploadDTO(url, code));
    }

    public ResponseEntity<Void> fileStoredInCloud(String code){
        if(!this.fileExists(code)) throw new FileNotExistsInCloudException("File " + code + " doesn't exist!");
        String url = this.reqUrlPreSigned(code);
        redisTemplate.opsForValue().set(REDIS_CODE_FILE.concat(code.split("\\.")[0]), url, delay, TimeUnit.MILLISECONDS);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("x-delay", delay);

        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
        Message message = messageConverter.toMessage(new DeleteMessageDTO(code), messageProperties);
        rabbitTemplate.convertAndSend(RABBITMQ_TOPIC_EXCHANGE, RABBITMQ_BINDING_DELETE, message);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<UploadDTO> reqUrlToDownload(String code) {
        var fileExists = redisTemplate.opsForValue().get(REDIS_CODE_FILE.concat(code));
        if(ObjectUtils.isEmpty(fileExists)) throw new FileNameNotFoundInRedisException();
        return ResponseEntity.ok(new UploadDTO(fileExists, code));
    }

    // Get pre-signed url to download file.
    private String reqUrlPreSigned(String code) throws FileNameNotFoundInRedisException {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(CLOUDFLARE_BUCKET_NAME)
                .key(code)
                .build();

        GetObjectPresignRequest preSignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMillis(delay)) // The URL will expire in 5 minutes.
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest preSignedRequest = preSigner.presignGetObject(preSignRequest);
        return preSignedRequest.url().toExternalForm();
    }
    //check if file exists in cloud
    private boolean fileExists(String code){
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(CLOUDFLARE_BUCKET_NAME)
                .key(code).build();
        return s3Client.getObject(request) != null;
    }
}
