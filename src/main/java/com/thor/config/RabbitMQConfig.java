package com.thor.config;
import com.thor.util.MQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 集中声明所有需要用到的队列 (Queue)
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 声明订单队列
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(MQConstants.orderQueue, true);
    }

    /**
     * 声明订单回滚队列
     */
    @Bean
    public Queue rollbackQueue() {
        // 注意这里监听器用的是 MQConstants.rollBack，所以队列名也用它
        return new Queue(MQConstants.rollBack, true);
    }

    /**
     * 声明评论队列
     */
    @Bean
    public Queue commentQueue() {
        return new Queue(MQConstants.commentQueue, true);
    }

    // 注意：如果项目中还定义了交换机（Exchange）和绑定（Binding），
    // 它们也需要在这里被声明为 @Bean。
    // 如果没有，只声明队列就可以了。
}