package br.com.washington.cloudflare_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${env-variable.rabbitmq.topic-exchange}")
    private String RABBITMQ_TOPIC_EXCHANGE;

    @Value("${env-variable.rabbitmq.queue-delete}")
    private String RABBITMQ_QUEUE_DELETE;

    @Value("${env-variable.rabbitmq.binding-delete}")
    private String RABBITMQ_BINDING_DELETE;

    // Message Converter Bean
    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitListener Container Factory with JSON message conversion
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // Declare the Queue
    @Bean
    Queue deleteFileQueue() {
        return new Queue(RABBITMQ_QUEUE_DELETE, true);
    }

    // Declare the Delayed Exchange (CustomExchange)
    @Bean
    CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct"); // The type of the underlying exchange
        return new CustomExchange(RABBITMQ_TOPIC_EXCHANGE, "x-delayed-message", true, false, args);
    }

    // Binding the Queue to the Delayed Exchange
    @Bean
    Binding bindingDeleteFile(Queue deleteFileQueue, CustomExchange delayExchange) {
        return BindingBuilder.bind(deleteFileQueue).to(delayExchange).with(RABBITMQ_BINDING_DELETE).noargs();
    }
}
