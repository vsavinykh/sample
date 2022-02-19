package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.SentDeductionRecord
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Transactional
interface DeductionsRepository {

    suspend fun save(employerId: String, employeeId: Long, payrollId: Long, deductionId: String, payDate: LocalDate, amount: Long, isSent: Boolean)
    suspend fun findAll() : List<SentDeductionRecord>
    suspend fun findAllByBetweenDayBeforeAndSentDateAndIsSentFalse(sentDate: LocalDateTime) : List<SentDeductionRecord>
}