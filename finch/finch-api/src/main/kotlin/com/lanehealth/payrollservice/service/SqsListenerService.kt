package com.lanehealth.payrollservice.service

import com.lanehealth.payrollservice.finch.contract.SqsType
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SqsListenerService(
    val employeeFinchSynchronizationService: EmployeeFinchSynchronizationService,
    val deductionsService: DeductionsService
) {

    @SqsListener("\${finchQueue}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    fun readEmployeeQueue(@Header("MessageGroupId") messageGroupId: String, message: String)= runBlocking {
        logger.info { "sqs listener message group id = $messageGroupId" }
        when (messageGroupId) {
            SqsType.IMPORT_EMPLOYEE.name -> employeeFinchSynchronizationService.processImportEmployee(message)
            SqsType.IMPORT_DEDUCTION.name -> deductionsService.processImportDeduction(message)
            else -> logger.warn { "cant process, messageGroupId $messageGroupId is not registered" }
        }
    }
}