package com.lanehealth.payrollservice.service.grpc

import com.google.protobuf.Empty
import com.lanehealth.payrollservice.finch.contract.UpdateEmployeesRequest
import com.lanehealth.payrollservice.finch.contract.UpdateEmployeesServiceGrpcKt
import com.lanehealth.payrollservice.service.EmployeeFinchSynchronizationService
import kotlinx.coroutines.Dispatchers
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class TriggerGrpcService(
    private val employeeFinchSynchronizationService: EmployeeFinchSynchronizationService
) : UpdateEmployeesServiceGrpcKt.UpdateEmployeesServiceCoroutineImplBase(
    Dispatchers.Unconfined
) {
    override suspend fun askForUpdateEmployees(request: UpdateEmployeesRequest): Empty {
        employeeFinchSynchronizationService.processEmployeesFromFinch(request.employerId)
        employeeFinchSynchronizationService.updateLHEmployees()
        return Empty.newBuilder().build()
    }
}