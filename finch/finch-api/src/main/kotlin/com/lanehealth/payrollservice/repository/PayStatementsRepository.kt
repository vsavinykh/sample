package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.PayStatementsRecord
import org.jooq.Record5
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional
interface PayStatementsRepository {
    suspend fun savePayStatements(pays: List<PayStatementsRecord>)
    suspend fun findAll(): List<PayStatementsRecord>
    suspend fun findAllByPaymentIdPageable(paymentId: Long): List<Record5<Long, Long, String, LocalDate, Long>>
    suspend fun findAllByEmployeeIdPageable(employeeId: Long): List<Record5<Long, Long, String, LocalDate, Long>>
    suspend fun findAllPayStatementsByPaymentIds(paymentIds: List<Long>): List<Record5<Long, Long, String, LocalDate, Long>>
}