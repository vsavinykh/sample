package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.Tables.FINCH_ACCESS_TOKEN
import com.lanehealth.payroll.model.generated.tables.records.FinchAccessTokenRecord
import com.lanehealth.payrollservice.repository.FinchAccessTokenRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FinchAccessTokenRepositoryImpl(
    private val cf: ConnectionFactory
) : FinchAccessTokenRepository {

    @Transactional
    override suspend fun findAllByEmployerIdAndIsActive(employerId: Long, isActive: Boolean): List<FinchAccessTokenRecord> {
        return cf.toDsl().selectFrom(FINCH_ACCESS_TOKEN)
            .where(FINCH_ACCESS_TOKEN.EMPLOYER_ID.eq(employerId))
            .and(FINCH_ACCESS_TOKEN.IS_ACTIVE.eq(isActive))
            .awaitAll()
    }

    override suspend fun save(tokenRecord: FinchAccessTokenRecord) {
        cf.toDsl().insertInto(FINCH_ACCESS_TOKEN).set(tokenRecord).awaitSingle()
    }

    override suspend fun update(tokenRecord: FinchAccessTokenRecord) {
        cf.toDsl().update(FINCH_ACCESS_TOKEN).set(tokenRecord).where(FINCH_ACCESS_TOKEN.ID.eq(tokenRecord.id)).awaitSingle()
    }

    @Transactional
    override suspend fun findOneActiveTokenByEmployerId(employerId: Long): FinchAccessTokenRecord? {
        return cf.toDsl().selectFrom(FINCH_ACCESS_TOKEN)
            .where(FINCH_ACCESS_TOKEN.EMPLOYER_ID.eq(employerId))
            .and(FINCH_ACCESS_TOKEN.IS_ACTIVE.eq(true))
            .awaitFirstOrNull()
    }
}