package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static String UPLOAD_QUEUE= "upload.queue";
    public static String UPLOAD_ROUTING_KEY= "upload.routing.key";
    public static String UPLOAD_EXCHANGE= "upload.exchange";
    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }


    @Bean
    public Queue uploadQueue() {
        return new Queue(UPLOAD_QUEUE, true);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange uploadExchange() {
        return new DirectExchange(UPLOAD_EXCHANGE, true, false);
    }

    @Bean
    public Binding uploadBinding(Queue uploadQueue, DirectExchange uploadExchange) {
        return BindingBuilder.bind(uploadQueue)
                             .to(uploadExchange)
                             .with(UPLOAD_ROUTING_KEY);
    }


}
