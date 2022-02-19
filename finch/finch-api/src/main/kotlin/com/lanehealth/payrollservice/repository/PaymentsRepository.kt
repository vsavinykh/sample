package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.PaymentsRecord
import org.springframework.transaction.annotation.Transactional

@Transactional
interface PaymentsRepository {
    suspend fun findAll(): List<PaymentsRecord>
    suspend fun findOneByPaymentId(paymentId: String): PaymentsRecord?
    suspend fun savePayments(payments: List<PaymentsRecord>)
    suspend fun findAllByEmployerIdPageable(employerId: Long): List<PaymentsRecord>
}