package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import org.springframework.transaction.annotation.Transactional

@Transactional
interface EmployerRepository {
    suspend fun findAll() : List<EmployerRecord>
    suspend fun findById(employerId: Long) : EmployerRecord?
    suspend fun findByLhEmployerId(lhEmployerId: String) : EmployerRecord?
    suspend fun save(employerRecord: EmployerRecord)
    suspend fun update(employerRecord: EmployerRecord)
}