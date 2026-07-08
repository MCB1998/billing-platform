package com.mcb.billing.invoice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ setup for publishing domain events. Declares the shared topic exchange
 * and a JSON message converter (using Spring Boot's ObjectMapper) so events are
 * sent as JSON.
 */
@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange billingEventsExchange(@Value("${billing.messaging.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
