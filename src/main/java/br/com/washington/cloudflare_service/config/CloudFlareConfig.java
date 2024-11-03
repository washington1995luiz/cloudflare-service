package br.com.washington.cloudflare_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class CloudFlareConfig {

    @Value("${env-variable.cloudflare.url}")
    public String CLOUDFLARE_URL = "";

    @Value("${env-variable.cloudflare.access-key}")
    public String CLOUDFLARE_ACCESS_KEY = "";

    @Value("${env-variable.cloudflare.secret}")
    public String CLOUDFLARE_SECRET = "";

    @Value("${env-variable.cloudflare.region}")
    public String CLOUDFLARE_REGION = "";

    @Bean
    S3Client s3Client(){
        return S3Client.builder()
                .endpointOverride(URI.create(CLOUDFLARE_URL))
                .region(Region.of(CLOUDFLARE_REGION))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        CLOUDFLARE_ACCESS_KEY,
                                        CLOUDFLARE_SECRET
                                )
                        )).build();
    }

    @Bean
    S3Presigner s3Presigner(){
        return S3Presigner.builder()
                .endpointOverride(URI.create(CLOUDFLARE_URL))
                .region(Region.of(CLOUDFLARE_REGION))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        CLOUDFLARE_ACCESS_KEY,
                                        CLOUDFLARE_SECRET
                                ))).build();
    }
}
