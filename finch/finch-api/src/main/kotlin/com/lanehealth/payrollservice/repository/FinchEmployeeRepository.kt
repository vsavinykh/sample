package com.lanehealth.payrollservice.repository

import com.lanehealth.payroll.model.generated.tables.records.FinchEmployeeRecord
import com.lanehealth.payrollservice.finch.contract.FinchIndividualId
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional
interface FinchEmployeeRepository {
    suspend fun saveFinchEmployees(finchEmployees: List<FinchEmployeeRecord>)
    suspend fun findOneByFieldsOrSSn(firstName:String, lastName:String, dob:LocalDate, ssn:String) : FinchEmployeeRecord?
    suspend fun findOneByLHEmployeeId(lhEmployeeId: Long) : FinchEmployeeRecord?
    suspend fun findIndividualIdsByLHEmployeeIds(lhEmployeeIds: List<Long>) : List<FinchIndividualId>
    suspend fun findAllIndividualIds() : List<String>
    suspend fun findAll(): List<FinchEmployeeRecord>
}