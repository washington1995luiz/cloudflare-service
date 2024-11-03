package br.com.washington.cloudflare_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.logging.Logger;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${env-variable.cors.originPatterns:default}")
    private String corsOriginPatterns = "";

    private Logger logger = Logger.getLogger(WebConfig.class.getName());

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var allowedOrigins = corsOriginPatterns.split(",");
        logger.info(Arrays.toString(allowedOrigins));
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST")
                .allowedOriginPatterns(allowedOrigins)
                .allowCredentials(true);
    }
}
