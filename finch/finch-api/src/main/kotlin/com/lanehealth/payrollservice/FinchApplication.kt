package com.lanehealth.payrollservice

import com.lanehealth.payrollservice.config.additionalBeans
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import reactor.core.publisher.Hooks

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
class FinchApplication

fun main(args: Array<String>) {
    Hooks.onOperatorDebug();
    runApplication<FinchApplication>(*args) {
        addInitializers(additionalBeans)
    }
}