package com.lanehealth.payrollservice.service.grpc

import com.google.protobuf.Empty
import com.lanehealth.payrollservice.finch.contract.*
import com.lanehealth.payrollservice.service.PaymentsService
import kotlinx.coroutines.Dispatchers
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class PaymentsGrpcService(
    val paymentsService: PaymentsService
) : PaymentsServiceGrpcKt.PaymentsServiceCoroutineImplBase(
    Dispatchers.Unconfined
) {

    override suspend fun processFinchPayments(request: PaymentsRequest): Empty {
        request.paymentList.forEach {
            paymentsService.processPayments(it.employerId, it.startDate, it.endDate) }
        return Empty.newBuilder().build()
    }

    override suspend fun askForFinchPaymentsByEmployer(request: FinchPaymentRequest): FinchPaymentsByEmployerResponse {
        return paymentsService.askForFinchPaymentsByEmployer(request)
    }

    override suspend fun askForFinchPayStatementsByPayment(request: FinchPayStatementRequest): FinchPayStatementsResponse {
        return paymentsService.askForFinchPayStatementsByPayment(request)
    }

    override suspend fun askForFinchPayStatementsByEmployee(request: FinchPayStatementRequest): FinchPayStatementsResponse {
        return paymentsService.askForFinchPayStatementsByEmployee(request)
    }

    override suspend fun askForFinchPayStatementsByPaymentIds(request: FinchPayStatementByPaymentsRequest): FinchPayStatementsResponse {
        return paymentsService.askForFinchPayStatementsByPaymentIds(request)
    }
}