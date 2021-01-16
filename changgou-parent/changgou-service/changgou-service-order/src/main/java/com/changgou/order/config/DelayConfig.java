package com.changgou.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * @Auther lxy
 * @Date
 */
@Configuration
public class DelayConfig {
/**
*创建一个正常队列
 * @return : org.springframework.amqp.core.Queue
 */
    @Bean
    public Queue createNorQueue() {
        return QueueBuilder.durable("normal_queue").build();
    }
/**
*
创建死信队列
 * @return : org.springframework.amqp.core.Queue
 */
    @Bean
    public Queue createDeadQueue() {
        return QueueBuilder.durable("dead_queue")
                .withArgument("x-dead-letter-exchange","exchange_order_delay")//设置死信交换机
                .withArgument("x-dead-letter-routing-key","dead.order")//设置死信路由key
                .build();
    }
/**
*
创建死信交换机
 * @return : org.springframework.amqp.core.DirectExchange
 */
    @Bean
    public DirectExchange createOrderExchangeDelay() {
        return new DirectExchange("exchange_order_delay");
    }
/**
*
绑定正常队列到死信交换机
 * @return : org.springframework.amqp.core.Binding
 */
    @Bean
    public Binding createBinding() {
        return BindingBuilder.bind(createNorQueue()).to(createOrderExchangeDelay()).with("dead.order");
    }
}
