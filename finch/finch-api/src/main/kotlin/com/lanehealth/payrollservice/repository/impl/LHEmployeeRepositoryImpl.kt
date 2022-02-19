package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.tables.LhEmployee.LH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.records.LhEmployeeRecord
import com.lanehealth.payrollservice.repository.LHEmployeeRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class LHEmployeeRepositoryImpl(
    private val cf: ConnectionFactory
) : LHEmployeeRepository {

    @Transactional
    override suspend fun insert(employees: List<LhEmployeeRecord>){
        logger.info { "count of input employees = ${employees.size}" }
        employees.forEach {
            cf.toDsl()
                .insertInto(
                    LH_EMPLOYEE,
                    LH_EMPLOYEE.FIRST_NAME,
                    LH_EMPLOYEE.MIDDLE_NAME,
                    LH_EMPLOYEE.LAST_NAME,
                    LH_EMPLOYEE.DATE_OF_BIRTHDAY,
                    LH_EMPLOYEE.SSN,
                    LH_EMPLOYEE.LANE_EMPLOYEE_ID,
                    LH_EMPLOYEE.DIVISION,
                    LH_EMPLOYEE.EMPLOYER_ID,
                    LH_EMPLOYEE.PAYROLL_ID
                )
                .values(
                    it.firstName,
                    it.middleName,
                    it.lastName,
                    it.dateOfBirthday,
                    it.ssn,
                    it.laneEmployeeId,
                    it.division,
                    it.employerId,
                    it.payrollId
                )
                .onDuplicateKeyIgnore().awaitSingle()
        }
    }

    override suspend fun findOneByLHEmployeeId(laneEmployeeId: Long): LhEmployeeRecord? {
        return cf.toDsl().selectFrom(LH_EMPLOYEE).where(LH_EMPLOYEE.LANE_EMPLOYEE_ID.eq(laneEmployeeId))
            .awaitFirstOrNull()
    }

    @Transactional
    override suspend fun findAllWhereFinchIdIsNull(): List<LhEmployeeRecord> {
        return cf.toDsl().selectFrom(LH_EMPLOYEE).where(LH_EMPLOYEE.FINCH_EMPLOYEE_ID.isNull)
            .awaitAll()
    }

    override suspend fun findAll(): List<LhEmployeeRecord> {
        return cf.toDsl().selectFrom(LH_EMPLOYEE).awaitAll()
    }

    @Transactional
    override suspend fun batchUpdate(employees: List<LhEmployeeRecord>) {
        logger.info { "count of updated employees = ${employees.size}" }
        cf.toDsl().batch(
            employees.map { cf.toDsl().update(LH_EMPLOYEE).set(it).where(LH_EMPLOYEE.ID.eq(it.id)) }
        ).awaitAll()
    }

    override suspend fun findAllByPayrollIdAndNotInEmployeeIds(payrollId: Long, employeeIds: List<Long>): List<LhEmployeeRecord> {
        return cf.toDsl().selectFrom(LH_EMPLOYEE)
            .where(LH_EMPLOYEE.PAYROLL_ID.eq(payrollId))
            .and(LH_EMPLOYEE.LANE_EMPLOYEE_ID.notIn(employeeIds))
            .awaitAll()
    }
}