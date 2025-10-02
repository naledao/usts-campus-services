package hhsc.kangnasi.xyz.ustscampusservices.service;

import hhsc.kangnasi.xyz.ustscampusservices.config.MqConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mq.delay.enabled", havingValue = "true")
public class DelayedMessageListener {

    @RabbitListener(queues = MqConstants.BIZ_QUEUE)
    public void onMessage(Object payload) {
        System.out.println("[DelayedMessageListener] Received at " + System.currentTimeMillis() + ": " + payload);
    }
}

