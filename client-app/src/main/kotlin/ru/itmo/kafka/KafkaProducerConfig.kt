package ru.itmo.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonSerializer
import ru.itmo.messages.BorrowerData
import ru.itmo.messages.LoanRequestDto
import ru.itmo.messages.PaymentDto

@Configuration
class KafkaProducerConfig {
    @Value("\${kafka.server}")
    private val kafkaServer: String? = null

    @Value("\${kafka.producer.id}")
    private val kafkaProducerId: String = ""

    @Bean
    fun producerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServer!!
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = LongSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        props[ProducerConfig.CLIENT_ID_CONFIG] = kafkaProducerId
        return props
    }

    @Bean
    fun producerBorrowerDataFactory(): ProducerFactory<Long, BorrowerData> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaBorrowerDataTemplate(): KafkaTemplate<Long, BorrowerData> {
        val template: KafkaTemplate<Long, BorrowerData> = KafkaTemplate(producerBorrowerDataFactory())
        template.setMessageConverter(StringJsonMessageConverter())
        return template
    }

    @Bean
    fun producerPaymentFactory(): ProducerFactory<Long, PaymentDto> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaPaymentTemplate(): KafkaTemplate<Long, PaymentDto> {
        val template: KafkaTemplate<Long, PaymentDto> = KafkaTemplate(producerPaymentFactory())
        template.setMessageConverter(StringJsonMessageConverter())
        return template
    }

    @Bean
    fun producerLoanRequestFactory(): ProducerFactory<Long, LoanRequestDto> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaLoanRequestTemplate(): KafkaTemplate<Long, LoanRequestDto> {
        val template: KafkaTemplate<Long, LoanRequestDto> = KafkaTemplate(producerLoanRequestFactory())
        template.setMessageConverter(StringJsonMessageConverter())
        return template
    }
}
