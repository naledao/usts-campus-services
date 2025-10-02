package hhsc.kangnasi.xyz.ustscampusservices.config;

import hhsc.kangnasi.xyz.ustscampusservices.contant.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "mq.delay.enabled", havingValue = "true")
public class RabbitDelayConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Delay exchange/queue (TTL + DLX, per-message TTL)
    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(MqConstant.DELAY_EXCHANGE, true, false);
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", MqConstant.BIZ_EXCHANGE);
        args.put("x-dead-letter-routing-key", MqConstant.BIZ_ROUTING_KEY);
        return new Queue(MqConstant.DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(MqConstant.DELAY_ROUTING_KEY);
    }

    // Business exchange/queue (receives expired messages from delay queue)
    @Bean
    public DirectExchange bizExchange() {
        return new DirectExchange(MqConstant.BIZ_EXCHANGE, true, false);
    }

    @Bean
    public Queue bizQueue() {
        return new Queue(MqConstant.BIZ_QUEUE, true);
    }

    @Bean
    public Binding bizBinding(Queue bizQueue, DirectExchange bizExchange) {
        return BindingBuilder.bind(bizQueue).to(bizExchange).with(MqConstant.BIZ_ROUTING_KEY);
    }
}

