RabbitMQ per-message delay options

1) Without plugin: TTL + DLX (implemented)
- Publish to a delay queue and set per-message TTL via the `expiration` property (milliseconds).
- The delay queue has `x-dead-letter-exchange`/`x-dead-letter-routing-key` so expired messages route to the business queue.
- Caveat: head-of-line blocking. A long TTL message at the head of the delay queue can delay shorter TTL messages behind it.

2) With plugin: Delayed Message Exchange (recommended for strict scheduling)
- Requires installing `rabbitmq_delayed_message_exchange` on the broker.
- Declare an exchange of type `x-delayed-message` and set header `x-delay` per message.
- Avoids head-of-line blocking and supports arbitrary per-message delays.

Project usage (TTL + DLX)
- Enable via config: set `mq.delay.enabled=true` and configure `spring.rabbitmq.*` in your active profile YAML.
- Send: call `GET /usts-campus-services/mq/delay/send?msg=hello&delayMs=5000`.
- Consume: see `hhsc/kangnasi/xyz/ustscampusservices/service/DelayedMessageListener.java` which logs received payloads.

Exchange/Queue names
- Delay exchange/queue/rk: `demo.delay.exchange` / `demo.delay.queue` / `demo.delay.key`
- Biz exchange/queue/rk: `demo.biz.exchange` / `demo.biz.queue` / `demo.biz.key`

Switching to the plugin approach (snippet)
```java
// Exchange: new CustomExchange("demo.delay.exchange", "x-delayed-message", true, false, Map.of("x-delayed-type", "direct"));
// Send: rabbitTemplate.convertAndSend(EX, RK, payload, m -> { m.getMessageProperties().setHeader("x-delay", delayMs); return m; });
```

