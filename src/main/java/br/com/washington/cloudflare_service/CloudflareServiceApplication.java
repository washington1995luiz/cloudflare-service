package br.com.washington.cloudflare_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudflareServiceApplication {



	public static void main(String[] args) {
		SpringApplication.run(CloudflareServiceApplication.class, args);


	}

}
