package com.lanehealth.payrollservice.config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.lanehealth.payrollservice.properties.AmazonProperties
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.GsonMessageConverter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.*


@Configuration
class AmazonConfig(
    private val amazonProperties: AmazonProperties
) {

    @Bean
    fun sqsClient(): AmazonSQSAsync {
        val credentialsProvider: AWSCredentialsProvider =
            if (Objects.nonNull(amazonProperties.localAwsProfile)) {
                ProfileCredentialsProvider(amazonProperties.localAwsProfile)
            } else {
                DefaultAWSCredentialsProviderChain.getInstance()
            }
        return AmazonSQSAsyncClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(amazonProperties.awsRegion)
            .build()
    }

    @Bean
    fun simpleMessageListenerContainerFactory(amazonSqs: AmazonSQSAsync): SimpleMessageListenerContainerFactory {
        val factory = SimpleMessageListenerContainerFactory()
        factory.setAmazonSqs(amazonSqs)
        factory.setAutoStartup(true)
        factory.setMaxNumberOfMessages(5)
        return factory
    }

    @Bean
    fun simpleMessageListenerContainer(amazonSQSAsync: AmazonSQSAsync): SimpleMessageListenerContainer {
        val simpleMessageListenerContainer = SimpleMessageListenerContainer()
        simpleMessageListenerContainer.setAmazonSqs(amazonSQSAsync)
        simpleMessageListenerContainer.setMessageHandler(queueMessageHandler())
        simpleMessageListenerContainer.setMaxNumberOfMessages(10)
        simpleMessageListenerContainer.setTaskExecutor(threadPoolTaskExecutor())
        return simpleMessageListenerContainer
    }

    @Bean
    fun queueMessageHandler(): QueueMessageHandler {
        val queueMessageHandlerFactory = QueueMessageHandlerFactory()
        queueMessageHandlerFactory.setAmazonSqs(sqsClient())
        val messageConverter = GsonMessageConverter()
        messageConverter.isStrictContentTypeMatch = false
        queueMessageHandlerFactory.messageConverters = listOf(messageConverter)
        return queueMessageHandlerFactory.createQueueMessageHandler()
    }

    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 10
        executor.initialize()
        return executor
    }
}