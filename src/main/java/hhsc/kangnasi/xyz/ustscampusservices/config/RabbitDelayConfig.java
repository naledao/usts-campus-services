package hhsc.kangnasi.xyz.ustscampusservices.config;

import hhsc.kangnasi.xyz.ustscampusservices.contant.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
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

    // Delay exchange using rabbitmq_delayed_message_exchange plugin
    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(MqConstant.DELAY_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue bizQueue() {
        return new Queue(MqConstant.BIZ_QUEUE, true);
    }

    @Bean
    public Binding bizBinding(Queue bizQueue, CustomExchange delayExchange) {
        // Bind business queue directly to the delayed exchange using the existing delay routing key
        return BindingBuilder.bind(bizQueue).to(delayExchange).with(MqConstant.DELAY_ROUTING_KEY).noargs();
    }
}
