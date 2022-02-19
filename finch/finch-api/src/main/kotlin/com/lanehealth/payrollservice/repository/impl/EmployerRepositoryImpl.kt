package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.Tables.EMPLOYER
import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import com.lanehealth.payrollservice.repository.EmployerRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployerRepositoryImpl (
    private val cf: ConnectionFactory
) : EmployerRepository {

    @Transactional
    override suspend fun findAll(): List<EmployerRecord> {
        return cf.toDsl().selectFrom(EMPLOYER)
            .awaitAll()
    }

    override suspend fun findById(employerId: Long): EmployerRecord? {
        return cf.toDsl().selectFrom(EMPLOYER)
            .where(EMPLOYER.ID.eq(employerId))
            .awaitFirstOrNull()
    }

    override suspend fun findByLhEmployerId(lhEmployerId: String): EmployerRecord? {
        return cf.toDsl().selectFrom(EMPLOYER)
            .where(EMPLOYER.LH_EMPLOYER_ID.eq(lhEmployerId))
            .awaitFirstOrNull()
    }

    override suspend fun save(employerRecord: EmployerRecord) {
        cf.toDsl().insertInto(EMPLOYER).set(employerRecord).awaitSingle()
    }

    override suspend fun update(employerRecord: EmployerRecord) {
        cf.toDsl().update(EMPLOYER).set(employerRecord).where(EMPLOYER.ID.eq(employerRecord.id)).awaitSingle()
    }
}