package hhsc.kangnasi.xyz.ustscampusservices.controller;

import hhsc.kangnasi.xyz.ustscampusservices.service.DelayedMessageSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mq/delay")
@ConditionalOnProperty(name = "mq.delay.enabled", havingValue = "true")
public class MqDelayDemoController {

    private final DelayedMessageSender sender;

    public MqDelayDemoController(DelayedMessageSender sender) {
        this.sender = sender;
    }

    @GetMapping("/send")
    public Map<String, Object> send(
            @RequestParam("msg") String msg,
            @RequestParam("delayMs") long delayMs
    ) {
        long now = System.currentTimeMillis();
        sender.send("[at " + now + "] " + msg, delayMs);
        Map<String, Object> res = new HashMap<>();
        res.put("status", "ok");
        res.put("queuedAt", now);
        res.put("delayMs", delayMs);
        return res;
    }
}

