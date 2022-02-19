package com.lanehealth.payrollservice.service.grpc

import com.lanehealth.payrollservice.finch.contract.FinchIndividualProfileRequest
import com.lanehealth.payrollservice.finch.contract.FinchIndividualProfileResponse
import com.lanehealth.payrollservice.finch.contract.FinchIndividualProfileServiceGrpcKt
import com.lanehealth.payrollservice.repository.FinchEmployeeRepository
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import net.devh.boot.grpc.server.service.GrpcService
import java.sql.Timestamp
import java.time.Instant

private val logger = KotlinLogging.logger {}

@GrpcService
class MonolithIndividualProfileService(
    private val finchEmployeeRepository: FinchEmployeeRepository
) : FinchIndividualProfileServiceGrpcKt.FinchIndividualProfileServiceCoroutineImplBase(
    Dispatchers.Unconfined
) {
    override suspend fun askForProfile(request: FinchIndividualProfileRequest): FinchIndividualProfileResponse {
        logger.info { "Got FinchIndividualProfile request for id ${request.employeeId}" }
        val employee = finchEmployeeRepository.findOneByLHEmployeeId(request.employeeId)
        logger.info { "Get employee $employee" }
        return if (employee != null) {
            val instant: Instant = Timestamp.valueOf(employee.dateOfBirthday.atStartOfDay()).toInstant()
            val dob = com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.epochSecond).build()
            FinchIndividualProfileResponse.newBuilder()
                .setFirstName(employee.firstName ?: "")
                .setMiddleName(employee.middleName ?: "")
                .setLastName(employee.lastName ?: "")
                .setSsn(employee.ssn ?: "")
                .setIndividualId(employee.individualId)
                .setDob(dob)
                .setLaneEmployeeId(request.employeeId).build()
        } else {
            FinchIndividualProfileResponse.newBuilder().build()
        }
    }
}