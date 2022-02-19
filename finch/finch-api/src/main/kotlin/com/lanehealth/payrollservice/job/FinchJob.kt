package com.lanehealth.payrollservice.job

import com.lanehealth.payroll.model.generated.tables.records.FinchRequestTokenRecord
import com.lanehealth.payrollservice.model.TokenStatus
import com.lanehealth.payrollservice.repository.FinchRequestTokenRepository
import com.lanehealth.payrollservice.service.DeductionsService
import com.lanehealth.payrollservice.service.EmployeeFinchSynchronizationService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Component
class FinchJob(
    val employeeFinchSynchronizationService: EmployeeFinchSynchronizationService,
    val deductionsService: DeductionsService,
    val finchRequestTokenRepository: FinchRequestTokenRepository
) {


    /**
     * Run on daily basis
     */
    @Scheduled(cron = "\${jobs.cron.map-finch-employees}", zone = "US/Central")
    fun mapFinchEmployees() = runBlocking {
        logger.info("Start mapFinchEmployeesJob")
        employeeFinchSynchronizationService.updateLHEmployees()
        logger.info("Finished mapFinchEmployeesJob")
    }


    /**
     * Run every hour
     */
    @Scheduled(fixedDelayString = "\${jobs.interval.token-expiring}")
    fun changeTokenStatus() = runBlocking {
        val activeRequestTokens: List<FinchRequestTokenRecord> = finchRequestTokenRepository.findAllByStatus(TokenStatus.ACTIVE);
        activeRequestTokens.forEach {
            val date: LocalDateTime = LocalDateTime.now(ZoneId.of("US/Central"))
            if(!date.isBefore(it.expiresOn)) {
                it.status = TokenStatus.EXPIRED.name
                it.updatedAt = LocalDateTime.now(ZoneId.of("UTC"))
                finchRequestTokenRepository.update(it)
            }
        }
    }
}





