package com.citrus.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import com.citrus.share.enums.ExchangeTypeEnum;
import com.citrus.share.enums.RabbitMQEnum;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig implements InitializingBean {

    private final RabbitAdmin rabbitAdmin;

    /**
     * 這個方法會在 Spring Bean 初始化完成後自動執行
     * 自動掃描 RabbitMQEnum 並創建 Exchange、Queue 和 Binding
     */
    @Override
    public void afterPropertiesSet() {
        for (RabbitMQEnum mq : RabbitMQEnum.values()) {
            // 1. 創建 Exchange
            Exchange exchange = createExchange(mq);
            rabbitAdmin.declareExchange(exchange);

            // 2. 創建 Queue 和 Binding
            for (String queueName : mq.getQueueMap().values()) {
                // 創建 Queue (durable = true)
                Queue queue = new Queue(queueName, true);
                rabbitAdmin.declareQueue(queue);

                // 創建 Binding
                Binding binding = createBinding(queue, exchange, mq);
                rabbitAdmin.declareBinding(binding);
            }
        }
    }

    /**
     * 根據 ExchangeTypeEnum 創建對應的 Exchange
     */
    private Exchange createExchange(RabbitMQEnum mq) {
        String exchangeName = mq.getExchangeName();
        ExchangeTypeEnum type = mq.getExchangeType();

        return switch (type) {
            case TOPIC -> new TopicExchange(exchangeName, true, false);
            case DIRECT -> new DirectExchange(exchangeName, true, false);
            case FANOUT -> new FanoutExchange(exchangeName, true, false);
            case HEADERS -> new HeadersExchange(exchangeName, true, false);
        };
    }

    /**
     * 根據 Exchange 類型創建對應的 Binding
     */
    private Binding createBinding(Queue queue, Exchange exchange, RabbitMQEnum mq) {
        ExchangeTypeEnum type = mq.getExchangeType();
        String routingKey = mq.getRoutingKey();

        return switch (type) {
            case TOPIC -> BindingBuilder
                    .bind(queue)
                    .to((TopicExchange) exchange)
                    .with(routingKey);

            case DIRECT -> BindingBuilder
                    .bind(queue)
                    .to((DirectExchange) exchange)
                    .with(routingKey);

            case FANOUT -> BindingBuilder
                    .bind(queue)
                    .to((FanoutExchange) exchange);

            case HEADERS -> throw new UnsupportedOperationException(
                    "Headers Exchange binding 需要自訂實作");
        };
    }
}
