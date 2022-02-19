package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.FinchRequestTokenRecord
import com.lanehealth.payrollservice.model.TokenStatus
import org.springframework.transaction.annotation.Transactional

@Transactional
interface FinchRequestTokenRepository {
   suspend fun findAllByEmployerIdAndStatus(employerId: Long, status: TokenStatus): List<FinchRequestTokenRecord>
   suspend fun save(tokenRecord: FinchRequestTokenRecord)
   suspend fun update(tokenRecord: FinchRequestTokenRecord)
   suspend fun findFirstByRequestTokenAndStatus(requestToken: String, status: TokenStatus): FinchRequestTokenRecord?
   suspend fun findAllByStatus(status: TokenStatus): List<FinchRequestTokenRecord>

}