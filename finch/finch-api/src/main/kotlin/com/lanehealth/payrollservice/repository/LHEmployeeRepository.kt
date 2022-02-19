package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.LhEmployeeRecord
import org.springframework.transaction.annotation.Transactional

@Transactional
interface LHEmployeeRepository {
    suspend fun insert(employees: List<LhEmployeeRecord>)
    suspend fun findAllWhereFinchIdIsNull(): List<LhEmployeeRecord>
    suspend fun  findAllByPayrollIdAndNotInEmployeeIds(payrollId: Long, employeeIds: List<Long>): List<LhEmployeeRecord>
    suspend fun findAll(): List<LhEmployeeRecord>
    suspend fun batchUpdate(employees: List<LhEmployeeRecord>)
    suspend fun findOneByLHEmployeeId(laneEmployeeId: Long): LhEmployeeRecord?
}