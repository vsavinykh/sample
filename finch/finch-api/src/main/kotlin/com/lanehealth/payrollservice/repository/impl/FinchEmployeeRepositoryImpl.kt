package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.tables.FinchEmployee.FINCH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.LhEmployee.LH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.records.FinchEmployeeRecord
import com.lanehealth.payrollservice.finch.contract.FinchIndividualId
import com.lanehealth.payrollservice.repository.FinchEmployeeRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class FinchEmployeeRepositoryImpl(
    private val cf: ConnectionFactory
) : FinchEmployeeRepository {

    @Transactional
    override suspend fun saveFinchEmployees(finchEmployees: List<FinchEmployeeRecord>) {
        cf.toDsl()
            .batch(finchEmployees.map { cf.toDsl().insertInto(FINCH_EMPLOYEE).set(it) })
            .awaitAll()
    }

    @Transactional
    override suspend fun findAllIndividualIds(): List<String> {
        return cf.toDsl().select(FINCH_EMPLOYEE.INDIVIDUAL_ID)
            .from(FINCH_EMPLOYEE)
            .awaitAll().map { it.value1() }
    }

    override suspend fun findOneByFieldsOrSSn(firstName: String, lastName: String, dob: LocalDate, ssn: String): FinchEmployeeRecord? {
        return cf.toDsl().selectFrom(FINCH_EMPLOYEE)
            .where(FINCH_EMPLOYEE.FIRST_NAME.eq(firstName))
            .and(FINCH_EMPLOYEE.LAST_NAME.eq(lastName))
            .and(FINCH_EMPLOYEE.DATE_OF_BIRTHDAY.eq(dob))
            .or(FINCH_EMPLOYEE.SSN.eq(ssn))
            .awaitFirstOrNull()
    }

    override suspend fun findOneByLHEmployeeId(lhEmployeeId: Long): FinchEmployeeRecord? {
        return cf.toDsl().selectFrom(FINCH_EMPLOYEE
            .join(LH_EMPLOYEE).on(FINCH_EMPLOYEE.ID.eq(LH_EMPLOYEE.FINCH_EMPLOYEE_ID)))
            .where(LH_EMPLOYEE.LANE_EMPLOYEE_ID.eq(lhEmployeeId))
            .awaitFirstOrNull()?.into(FINCH_EMPLOYEE)
    }

    override suspend fun findIndividualIdsByLHEmployeeIds(lhEmployeeIds: List<Long>): List<FinchIndividualId> {
        return cf.toDsl().select(FINCH_EMPLOYEE.INDIVIDUAL_ID, LH_EMPLOYEE.LANE_EMPLOYEE_ID)
            .from(FINCH_EMPLOYEE
            .join(LH_EMPLOYEE).on(FINCH_EMPLOYEE.ID.eq(LH_EMPLOYEE.FINCH_EMPLOYEE_ID)))
            .where(LH_EMPLOYEE.LANE_EMPLOYEE_ID.`in`(lhEmployeeIds))
            .awaitAll().map {
                FinchIndividualId.newBuilder().setIndividualId(it.value1()).setLhEmployeeId(it.value2()).build()
             }.toList()
    }

    override suspend fun findAll(): List<FinchEmployeeRecord> {
        return cf.toDsl().selectFrom(FINCH_EMPLOYEE).awaitAll()
    }
}