package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.tables.SentDeduction.SENT_DEDUCTION
import com.lanehealth.payroll.model.generated.tables.records.SentDeductionRecord
import com.lanehealth.payrollservice.repository.DeductionsRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import com.lanehealth.payrollservice.util.DateUtil
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.impl.DSL.name
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime


@Service
class DeductionsRepositoryImpl (
    private val cf: ConnectionFactory
) : DeductionsRepository {

    override suspend fun save(employerId: String, employeeId: Long, payrollId: Long,deductionId: String, payDate: LocalDate, amount: Long, isSent: Boolean) {
        cf.toDsl()
            .insertInto(
                SENT_DEDUCTION,
                SENT_DEDUCTION.EMPLOYER_ID,
                SENT_DEDUCTION.LH_EMPLOYEE_ID,
                SENT_DEDUCTION.DEDUCTION_ID,
                SENT_DEDUCTION.SENT_AT,
                SENT_DEDUCTION.PAY_DATE,
                SENT_DEDUCTION.PAYROLL_ID,
                SENT_DEDUCTION.AMOUNT,
                SENT_DEDUCTION.IS_SENT_TO_FINCH,
                SENT_DEDUCTION.CREATED_AT,
                SENT_DEDUCTION.UPDATED_AT
            )
            .values(
                employerId,
                employeeId,
                deductionId,
                DateUtil.nowUsCentralZone(),
                payDate,
                payrollId,
                amount,
                isSent,
                DateUtil.nowUsCentralZone(),
                DateUtil.nowUsCentralZone()
            )
            .onConflictOnConstraint(name("sent_deduction_unique"))
            .doUpdate()
            .set(SENT_DEDUCTION.UPDATED_AT, DateUtil.nowUsCentralZone())
            .set(SENT_DEDUCTION.SENT_AT, DateUtil.nowUsCentralZone())
            .set(SENT_DEDUCTION.IS_SENT_TO_FINCH, isSent)
            .set(SENT_DEDUCTION.AMOUNT, amount)
            .awaitSingle()
    }

    override suspend fun findAllByBetweenDayBeforeAndSentDateAndIsSentFalse(sentDate: LocalDateTime): List<SentDeductionRecord> {
        return cf.toDsl().selectFrom(SENT_DEDUCTION)
            .where(SENT_DEDUCTION.IS_SENT_TO_FINCH.isFalse)
            .and(SENT_DEDUCTION.SENT_AT.between(DateUtil.nowUsCentralZone().minusDays(1), DateUtil.nowUsCentralZone()))
            .awaitAll()
    }

    override suspend fun findAll(): List<SentDeductionRecord> {
        return cf.toDsl().selectFrom(SENT_DEDUCTION).awaitAll()
    }
}