package com.changgou.pay.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Auther lxy
 * @Date
 */
@Configuration
public class RabbitConfig {
/**
*创建队列
 * @return : org.springframework.amqp.core.Queue
 */
    @Bean(name = "queue1")
    public Queue createQueue() {
        return QueueBuilder.durable("order_queue").build();
    }
/**
*
创建交换机
 * @return : org.springframework.amqp.core.Exchange
 */
    @Bean(name = "exchange1")
    public Exchange createExchange() {
        return ExchangeBuilder.directExchange("order_exchange").build();
    }
/**
*绑定队列到交换机
 * @param queue :
 * @param exchange :
 * @return : org.springframework.amqp.core.Binding
 */
    @Bean(name = "binding1")
    public Binding createBinding(@Qualifier("queue1") Queue queue,@Qualifier("exchange1") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("order.add").noargs();
    }
}
