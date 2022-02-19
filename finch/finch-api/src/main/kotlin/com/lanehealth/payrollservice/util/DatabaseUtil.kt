package com.lanehealth.payrollservice.util

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.reactivestreams.Publisher
import org.springframework.r2dbc.connection.ConnectionFactoryUtils


object DatabaseUtil {
    suspend fun <T : Any> Publisher<T>.awaitAll(): List<T> = asFlow().toList()

    suspend fun ConnectionFactory.toDsl(): DSLContext =
        ConnectionFactoryUtils.getConnection(this).awaitFirst().let { DSL.using(it) }
}
