package com.lanehealth.payrollservice

import com.lanehealth.payrollservice.clients.FinchClient
import com.lanehealth.payrollservice.config.ContextInitializer
import com.lanehealth.payrollservice.finch.contract.ApiClient
import com.lanehealth.payrollservice.repository.*
import com.lanehealth.payrollservice.util.TestDataHelper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD
import io.r2dbc.spi.ConnectionFactoryOptions.USER
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Hooks

private val logger = KotlinLogging.logger {}

@SpringBootTest(properties = ["logging.level.com=INFO"])
@ExtendWith(MockKExtension::class)
@ContextConfiguration(initializers = [ContextInitializer::class])
@Import(Base.DslConfig::class)
abstract class Base {

    @MockkBean
    lateinit var finchClient: FinchClient
    @MockkBean
    lateinit var apiClient: ApiClient

    @Autowired
    lateinit var testDataHelper: TestDataHelper

    @Autowired
    lateinit var paymentsRepository: PaymentsRepository
    @Autowired
    lateinit var payStatementsRepository: PayStatementsRepository
    @Autowired
    lateinit var finchEmployeeRepository: FinchEmployeeRepository
    @Autowired
    lateinit var lhEmployeeRepository: LHEmployeeRepository
    @Autowired
    lateinit var employerRepository: EmployerRepository
    @Autowired
    lateinit var accessTokenRepository: FinchAccessTokenRepository
    @Autowired
    lateinit var deductionsRepository: DeductionsRepository

    @BeforeEach
    fun init() {
        Hooks.onOperatorDebug()
        every { finchClient.apiClient } answers { apiClient }
        every { finchClient.apiClient.setBearerToken(any()) } answers { }
    }

    @AfterEach
    fun afterEach() = runBlocking {
        val deleted = testDataHelper.clearAllTables()
        logger.info { "after tests deleted $deleted rows from db" }
    }

    @TestConfiguration
    class DslConfig {
        @Bean
        fun connectionFactory(environment: Environment): ConnectionFactory? {
            val options = ConnectionFactoryOptions.parse(environment.getRequiredProperty("spring.r2dbc.url"))
                .mutate()
                .option(USER, environment.getRequiredProperty("spring.r2dbc.username"))
                .option(PASSWORD, environment.getRequiredProperty("spring.r2dbc.password"))
                .build()
            return ConnectionFactories.get(options)
        }
        @Bean
        fun dslContext(cf: ConnectionFactory) = DSL.using(cf)
    }
}