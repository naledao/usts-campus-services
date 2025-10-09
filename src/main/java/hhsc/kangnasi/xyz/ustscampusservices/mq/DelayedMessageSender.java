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
     * Uses rabbitmq_delayed_message_exchange (x-delayed-message) with x-delay header.
     */
    public void send(Object payload, long delayMillis) {
        final long finalDelay = delayMillis < 0 ? 0 : delayMillis;

        MessagePostProcessor mpp = message -> {
            // x-delay expects milliseconds as a number; plugin will hold the message
            message.getMessageProperties().setHeader("x-delay", finalDelay);
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
