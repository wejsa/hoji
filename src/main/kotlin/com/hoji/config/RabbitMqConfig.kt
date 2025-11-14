package com.hoji.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * RabbitMQ 설정
 *
 * RabbitMQ를 사용하지 않는 경우 application.yml에서 다음을 추가:
 * spring.autoconfigure.exclude:
 *   - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(name = ["spring.rabbitmq.host"])
class RabbitMqConfig {

    companion object {
        // 예제 큐 설정
        const val EXAMPLE_QUEUE = "hoji.example.queue"
        const val EXAMPLE_EXCHANGE = "hoji.example.exchange"
        const val EXAMPLE_ROUTING_KEY = "hoji.example.routing.key"
    }

    /**
     * Jackson Message Converter
     */
    @Bean
    fun jackson2JsonMessageConverter(): Jackson2JsonMessageConverter {
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
        return Jackson2JsonMessageConverter(objectMapper)
    }

    /**
     * RabbitTemplate
     */
    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: Jackson2JsonMessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    /**
     * Listener Container Factory
     */
    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        messageConverter: Jackson2JsonMessageConverter
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(messageConverter)
        }
    }

    /**
     * 예제 큐
     */
    @Bean
    fun exampleQueue(): Queue {
        return QueueBuilder.durable(EXAMPLE_QUEUE).build()
    }

    /**
     * 예제 Exchange
     */
    @Bean
    fun exampleExchange(): TopicExchange {
        return ExchangeBuilder.topicExchange(EXAMPLE_EXCHANGE).durable(true).build()
    }

    /**
     * 예제 Binding
     */
    @Bean
    fun exampleBinding(exampleQueue: Queue, exampleExchange: TopicExchange): Binding {
        return BindingBuilder.bind(exampleQueue).to(exampleExchange).with(EXAMPLE_ROUTING_KEY)
    }
}
