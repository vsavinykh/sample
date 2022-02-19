package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.tables.FinchEmployee.FINCH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.LhEmployee.LH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.PayStatements.PAY_STATEMENTS
import com.lanehealth.payroll.model.generated.tables.Payments.PAYMENTS
import com.lanehealth.payroll.model.generated.tables.records.PayStatementsRecord
import com.lanehealth.payrollservice.repository.PayStatementsRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitSingle
import mu.KotlinLogging
import org.jooq.Record5
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class PayStatementsRepositoryImpl(
    private val cf: ConnectionFactory
) : PayStatementsRepository {

    @Transactional
    override suspend fun savePayStatements(pays: List<PayStatementsRecord>) {
        logger.info { "saving pays size = ${pays.size}" }

        pays.forEach {
            cf.toDsl()
                .insertInto(
                    PAY_STATEMENTS,
                    PAY_STATEMENTS.PAYMENT_ID,
                    PAY_STATEMENTS.INDIVIDUAL_ID,
                    PAY_STATEMENTS.PAY_TYPE,
                    PAY_STATEMENTS.AMOUNT
                )
                .values(
                    it.paymentId,
                    it.individualId,
                    it.payType,
                    it.amount
                )
                .onDuplicateKeyIgnore()
                .awaitSingle()
        }
    }

    override suspend fun findAll(): List<PayStatementsRecord> {
        return cf.toDsl().selectFrom(PAY_STATEMENTS)
            .awaitAll()
    }

    override suspend fun findAllByPaymentIdPageable(paymentId: Long): List<Record5<Long, Long, String, LocalDate, Long>> {
        return cf.toDsl().select(PAY_STATEMENTS.ID, PAY_STATEMENTS.AMOUNT, PAY_STATEMENTS.PAY_TYPE, PAYMENTS.PAY_DATE, LH_EMPLOYEE.LANE_EMPLOYEE_ID)
            .from(PAY_STATEMENTS)
            .join(PAYMENTS).on(PAY_STATEMENTS.PAYMENT_ID.eq(PAYMENTS.PAYMENT_ID))
            .join(FINCH_EMPLOYEE).on(FINCH_EMPLOYEE.INDIVIDUAL_ID.eq(PAY_STATEMENTS.INDIVIDUAL_ID))
            .join(LH_EMPLOYEE).on(LH_EMPLOYEE.FINCH_EMPLOYEE_ID.eq(FINCH_EMPLOYEE.ID))
            .where(PAYMENTS.ID.eq(paymentId))
            .orderBy(PAYMENTS.ID.asc()).awaitAll()
    }

    override suspend fun findAllByEmployeeIdPageable(employeeId: Long): List<Record5<Long, Long, String, LocalDate, Long>> {
        return cf.toDsl().select(PAY_STATEMENTS.ID, PAY_STATEMENTS.AMOUNT, PAY_STATEMENTS.PAY_TYPE, PAYMENTS.PAY_DATE, LH_EMPLOYEE.LANE_EMPLOYEE_ID)
            .from(PAY_STATEMENTS)
            .join(PAYMENTS).on(PAY_STATEMENTS.PAYMENT_ID.eq(PAYMENTS.PAYMENT_ID))
            .join(FINCH_EMPLOYEE).on(PAY_STATEMENTS.INDIVIDUAL_ID.eq(FINCH_EMPLOYEE.INDIVIDUAL_ID))
            .join(LH_EMPLOYEE).on(LH_EMPLOYEE.FINCH_EMPLOYEE_ID.eq(FINCH_EMPLOYEE.ID))
            .where(LH_EMPLOYEE.ID.eq(employeeId))
            .orderBy(PAYMENTS.ID.asc()).awaitAll()
    }

    override suspend fun findAllPayStatementsByPaymentIds(paymentIds: List<Long>): List<Record5<Long, Long, String, LocalDate, Long>>  {
        return cf.toDsl().select(PAY_STATEMENTS.ID, PAY_STATEMENTS.AMOUNT, PAY_STATEMENTS.PAY_TYPE, PAYMENTS.PAY_DATE, LH_EMPLOYEE.LANE_EMPLOYEE_ID)
            .from(PAY_STATEMENTS)
            .join(PAYMENTS).on(PAY_STATEMENTS.PAYMENT_ID.eq(PAYMENTS.PAYMENT_ID))
            .join(FINCH_EMPLOYEE).on(PAY_STATEMENTS.INDIVIDUAL_ID.eq(FINCH_EMPLOYEE.INDIVIDUAL_ID))
            .join(LH_EMPLOYEE).on(LH_EMPLOYEE.FINCH_EMPLOYEE_ID.eq(FINCH_EMPLOYEE.ID))
            .where(PAYMENTS.ID.`in`(paymentIds)).awaitAll()
    }
}