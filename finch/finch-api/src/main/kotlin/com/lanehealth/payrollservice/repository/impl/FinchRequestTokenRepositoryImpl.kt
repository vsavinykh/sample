package com.lanehealth.payrollservice.repository.impl

import com.lanehealth.payroll.model.generated.Tables.FINCH_REQUEST_TOKEN
import com.lanehealth.payroll.model.generated.tables.records.FinchRequestTokenRecord
import com.lanehealth.payrollservice.model.TokenStatus
import com.lanehealth.payrollservice.repository.FinchRequestTokenRepository
import com.lanehealth.payrollservice.util.DatabaseUtil.awaitAll
import com.lanehealth.payrollservice.util.DatabaseUtil.toDsl
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service

@Service
class FinchRequestTokenRepositoryImpl(
    private val cf: ConnectionFactory
) : FinchRequestTokenRepository {
    override suspend fun findAllByEmployerIdAndStatus(employerId: Long, status: TokenStatus): List<FinchRequestTokenRecord> {
        return cf.toDsl().selectFrom(FINCH_REQUEST_TOKEN)
            .where(FINCH_REQUEST_TOKEN.EMPLOYER_ID.eq(employerId))
            .and(FINCH_REQUEST_TOKEN.STATUS.eq(status.name))
            .awaitAll()
    }

    override suspend fun save(tokenRecord: FinchRequestTokenRecord) {
        cf.toDsl().insertInto(FINCH_REQUEST_TOKEN).set(tokenRecord).awaitSingle()
    }

    override suspend fun update(tokenRecord: FinchRequestTokenRecord) {
        cf.toDsl().update(FINCH_REQUEST_TOKEN).set(tokenRecord).where(FINCH_REQUEST_TOKEN.ID.eq(tokenRecord.id)).awaitSingle()
    }

    override suspend fun findFirstByRequestTokenAndStatus(
        requestToken: String,
        status: TokenStatus
    ): FinchRequestTokenRecord? {
        return cf.toDsl().selectFrom(FINCH_REQUEST_TOKEN)
            .where(FINCH_REQUEST_TOKEN.REQUEST_TOKEN.eq(requestToken))
            .and(FINCH_REQUEST_TOKEN.STATUS.eq(status.name))
            .awaitFirstOrNull()
    }

    override suspend fun findAllByStatus(status: TokenStatus): List<FinchRequestTokenRecord>{
        return cf.toDsl().selectFrom(FINCH_REQUEST_TOKEN)
            .where(FINCH_REQUEST_TOKEN.STATUS.eq(status.name))
            .awaitAll()
    }
}