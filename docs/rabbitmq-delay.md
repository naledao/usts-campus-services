# RabbitMQ 延迟队列使用说明（按消息动态延迟）

更新：项目已切换为使用官方插件 rabbitmq_delayed_message_exchange（x-delayed-message）实现延迟投递；发送端通过设置消息头 x-delay（毫秒）控制延迟。下文保留 TTL+DLX 的说明以供对比与参考。

本文档说明如何在本项目中使用“每条消息可动态指定延迟”的消息投递能力。默认实现基于 RabbitMQ 的 TTL + DLX（无需安装插件），并提供切换到官方延迟交换机插件的参考。

## 功能概览

- 实现方式：延迟队列（TTL）+ 死信交换机（DLX），每条消息通过 `expiration` 属性设定毫秒级延迟。
- 默认关闭：通过开关 `mq.delay.enabled` 控制，避免本地无 RabbitMQ 时影响启动。
- JSON 序列化：使用 `Jackson2JsonMessageConverter`，可直接发送/消费对象。
- 示例接口与监听：内置演示 `GET /mq/delay/send`，以及一个简单消费者日志打印。

相关类与路径：
- 常量：`src/main/java/hhsc/kangnasi/xyz/ustscampusservices/config/MqConstants.java`
- 配置：`src/main/java/hhsc/kangnasi/xyz/ustscampusservices/config/RabbitDelayConfig.java`
- 发送：`src/main/java/hhsc/kangnasi/xyz/ustscampusservices/service/DelayedMessageSender.java`
- 消费：`src/main/java/hhsc/kangnasi/xyz/ustscampusservices/service/DelayedMessageListener.java`
- 演示接口：`src/main/java/hhsc/kangnasi/xyz/ustscampusservices/controller/MqDelayDemoController.java`

## 快速开始（TTL + DLX）

1. 启用功能开关（在当前激活的 profile 配置中）：

```yaml
mq:
  delay:
    enabled: true
```

2. 配置 RabbitMQ 连接（也可用环境变量覆盖）：

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: ${RABBITMQ_VHOST:/}
```

3. 启动应用（本地默认端口 `9880`，上下文路径 `/usts-campus-services`）。

4. 发送一条 5 秒延迟消息（示例）：

```bash
curl "http://localhost:9880/usts-campus-services/mq/delay/send?msg=hello&delayMs=5000"
```

5. 查看控制台日志，5 秒后可见消费者输出（示例）：

```
[DelayedMessageListener] Received at 1730000000000: [at 1729999995000] hello
```

## 编程方式使用（推荐在业务代码中集成）

在你的 Service 或 Controller 中注入 `DelayedMessageSender`，按消息设置不同的延迟：

```java
import hhsc.kangnasi.xyz.ustscampusservices.service.DelayedMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoBizService {
    private final DelayedMessageSender delayedMessageSender;

    public void scheduleRemind(String userId, long delayMs) {
        // 发送任意对象，已配置 Jackson JSON 转换
        var payload = java.util.Map.of("userId", userId, "type", "REMIND");
        delayedMessageSender.send(payload, delayMs); // 例如 3000L 表示 3 秒
    }
}
```

消费者实现（示例文件已提供，可按需替换为你的业务处理）：

```java
import hhsc.kangnasi.xyz.ustscampusservices.config.MqConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MyBizListener {
    @RabbitListener(queues = MqConstants.BIZ_QUEUE)
    public void onMessage(Object payload) {
        // TODO: 你的业务逻辑
        System.out.println("Biz consume: " + payload);
    }
}
```

## 队列与交换机（默认命名）

- 延迟交换机 / 队列 / 路由键：
  - `demo.delay.exchange` / `demo.delay.queue` / `demo.delay.key`
- 业务交换机 / 队列 / 路由键：
  - `demo.biz.exchange` / `demo.biz.queue` / `demo.biz.key`

如需更名，修改 `MqConstants` 中的常量并重启应用。

## 工作原理（TTL + DLX）

1. 发送消息到延迟交换机，消息属性设置 `expiration = <delayMs>`。
2. 消息进入延迟队列，直到 TTL 到期。
3. 到期后作为死信，按 `x-dead-letter-exchange` 和 `x-dead-letter-routing-key` 路由到业务交换机/队列。
4. 你的消费者从业务队列正常消费。

注意：TTL + DLX 受“队首阻塞（Head-of-Line Blocking）”影响——队首一条超长 TTL 的消息会阻塞后面短 TTL 消息的释放。若需要严格按条目精确调度，见下文插件方案。

## 常见问题与排查

- 未创建队列/交换机：确认 `mq.delay.enabled=true`；应用具备声明权限；RabbitMQ 可连接。
- 消息长时间未到达：
  - 是否被队首更长 TTL 的消息阻塞？
  - `delayMs` 是否设置过大（当前实现上限为 `Integer.MAX_VALUE` ≈ 24.8 天）？
- 连接失败：检查 `spring.rabbitmq.*` 配置及网络连通性/凭证/虚拟主机。
- 反序列化错误：检查消息体是否能被 Jackson 正确序列化/反序列化。
- 重启持久化：队列已配置为持久化；消息默认以持久化投递（由 Spring AMQP 模板控制）。

## 可选：使用延迟交换机插件（推荐消除队首阻塞）

如果你的 RabbitMQ 已安装官方插件 `rabbitmq_delayed_message_exchange`，可改用插件实现：

1. 在 Broker 启用插件（运维执行）：

```bash
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

2. Java 端切换（核心片段）：

```java
// 交换机类型改为 x-delayed-message，并指定底层路由类型（如 direct）
import org.springframework.amqp.core.CustomExchange;

@Bean
public CustomExchange delayExchange() {
    java.util.Map<String, Object> args = new java.util.HashMap<>();
    args.put("x-delayed-type", "direct");
    return new CustomExchange("demo.delay.exchange", "x-delayed-message", true, false, args);
}

// 直接将业务队列绑定到该延迟交换机（而非走 DLX）
@Bean
public Binding bizBinding(Queue bizQueue, CustomExchange delayExchange) {
    return BindingBuilder.bind(bizQueue).to(delayExchange).with("demo.biz.key").noargs();
}

// 发送时设置 x-delay 头部（而不是 expiration）
rabbitTemplate.convertAndSend(
    "demo.delay.exchange",
    "demo.biz.key",
    payload,
    m -> {
        m.getMessageProperties().setHeader("x-delay", delayMs);
        return m;
    }
);
```

该方案由交换机负责“持有并延迟投递”消息，可避免队首阻塞，更适合大规模、精确的延迟调度。

---

如需我为你切换到插件实现、或集成到具体业务流程，欢迎继续说明需求。
