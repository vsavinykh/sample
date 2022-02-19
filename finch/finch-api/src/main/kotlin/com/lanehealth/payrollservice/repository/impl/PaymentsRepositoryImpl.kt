package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.tables.Payments.PAYMENTS
import com.lanehealth.payroll.model.generated.tables.records.PaymentsRecord
import com.lanehealth.payrollservice.repository.PaymentsRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class PaymentsRepositoryImpl(
    private val cf: ConnectionFactory
) : PaymentsRepository {

    override suspend fun findOneByPaymentId(paymentId: String): PaymentsRecord? {
        return cf.toDsl().selectFrom(PAYMENTS).where(PAYMENTS.PAYMENT_ID.eq(paymentId))
            .awaitFirstOrNull()
    }

    override suspend fun findAll(): List<PaymentsRecord> {
        return cf.toDsl().selectFrom(PAYMENTS).awaitAll()
    }

    @Transactional
    override suspend fun savePayments(payments: List<PaymentsRecord>) {
        logger.info { "saving payments size = ${payments.size}" }
        payments.forEach {
            cf.toDsl()
                .insertInto(
                    PAYMENTS,
                    PAYMENTS.PAYMENT_ID,
                    PAYMENTS.START_DATE,
                    PAYMENTS.END_DATE,
                    PAYMENTS.PAY_DATE,
                    PAYMENTS.DEBIT_DATE,
                    PAYMENTS.EMPLOYER_ID
                )
                .values(
                    it.paymentId,
                    it.startDate,
                    it.endDate,
                    it.payDate,
                    it.debitDate,
                    it.employerId
                )
                .onDuplicateKeyIgnore().awaitSingle()
        }
    }

    override suspend fun findAllByEmployerIdPageable(employerId: Long): List<PaymentsRecord> {
        return cf.toDsl().selectFrom(PAYMENTS).where(PAYMENTS.EMPLOYER_ID.eq(employerId))
            .orderBy(PAYMENTS.ID.asc()).awaitAll()
    }
}