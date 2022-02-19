package com.lanehealth.payrollservice.config

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer

class ContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val db = PostgreSQLContainer<Nothing>("postgres:12").apply {
        withDatabaseName("finch_test")
        withUsername("postgres")
        withPassword("password")
        withEnv("PGDATA", "/tmp")
        withFileSystemBind("/tmp/", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)
    }

    override fun initialize(ac: ConfigurableApplicationContext) {
        db.start()
        System.setProperty("spring.r2dbc.url", "r2dbc:pool:postgresql://localhost:${db.firstMappedPort}/${db.databaseName}")
        System.setProperty("spring.r2dbc.username", db.username)
        System.setProperty("spring.r2dbc.password", db.password)
    }
}