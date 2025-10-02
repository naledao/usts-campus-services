package hhsc.kangnasi.xyz.ustscampusservices.mq;

import hhsc.kangnasi.xyz.ustscampusservices.contant.MqConstant;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "mq.delay.enabled", havingValue = "true")
public class DelayedMessageSender {

    private final RabbitTemplate rabbitTemplate;

    public DelayedMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send a message with a per-message delay (ms).
     * Uses TTL + Dead Letter Exchange to deliver after the delay.
     */
    public void send(Object payload, long delayMillis) {
        if (delayMillis < 0) delayMillis = 0;
        long finalDelay = Math.min(delayMillis, Integer.MAX_VALUE); // expiration is a String of integer milliseconds

        MessagePostProcessor mpp = message -> {
            message.getMessageProperties().setExpiration(String.valueOf(finalDelay));
            return message;
        };

        rabbitTemplate.convertAndSend(
                MqConstant.DELAY_EXCHANGE,
                MqConstant.DELAY_ROUTING_KEY,
                payload,
                mpp
        );
    }
}

