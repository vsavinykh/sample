package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.FinchAccessTokenRecord
import org.springframework.transaction.annotation.Transactional

@Transactional
interface FinchAccessTokenRepository {
    suspend fun findAllByEmployerIdAndIsActive(employerId: Long, isActive: Boolean) : List<FinchAccessTokenRecord>
    suspend fun findOneActiveTokenByEmployerId(employerId: Long): FinchAccessTokenRecord?
    suspend fun save(tokenRecord: FinchAccessTokenRecord)
    suspend fun update(tokenRecord: FinchAccessTokenRecord)
}