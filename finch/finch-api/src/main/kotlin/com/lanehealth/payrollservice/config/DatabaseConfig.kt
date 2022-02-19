package com.lanehealth.payrollservice.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class DatabaseConfig {
    @Bean(initMethod = "migrate")
    fun flyway(r2dbcProperties: R2dbcProperties): Flyway {
        val url = "jdbc:" + r2dbcProperties.url.substring("r2dbc:pool:".length)
        val user = r2dbcProperties.username
        val password = r2dbcProperties.password
        val config = Flyway
            .configure()
            .dataSource(url, user, password)
        return Flyway(config)
    }
}