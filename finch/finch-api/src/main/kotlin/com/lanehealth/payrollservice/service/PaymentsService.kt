package com.lanehealth.payrollservice.service

import com.lanehealth.payrollservice.clients.FinchClient
import com.lanehealth.payrollservice.finch.contract.*
import com.lanehealth.payrollservice.finch.contract.models.PayStatementRequest
import com.lanehealth.payrollservice.finch.contract.models.PayStatementRequestRequests
import com.lanehealth.payrollservice.mapper.FinchAccessTokenMapper
import com.lanehealth.payrollservice.mapper.PaymentsMapper
import com.lanehealth.payrollservice.properties.FinchProperties
import com.lanehealth.payrollservice.repository.EmployerRepository
import com.lanehealth.payrollservice.repository.FinchAccessTokenRepository
import com.lanehealth.payrollservice.repository.PayStatementsRepository
import com.lanehealth.payrollservice.repository.PaymentsRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import mu.KotlinLogging
import org.jooq.Record5
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class PaymentsService(
    val finchClient: FinchClient,
    val finchProps: FinchProperties,
    val accessTokenRepository: FinchAccessTokenRepository,
    val paymentsRepository: PaymentsRepository,
    val payStatementsRepository: PayStatementsRepository,
    val finchAccessTokenMapper: FinchAccessTokenMapper,
    val paymentsMapper: PaymentsMapper,
    val employerRepository: EmployerRepository
) {

    suspend fun processPayments(lhEmployerId: String, startDate: String, endDate: String) {
        val employer = employerRepository.findByLhEmployerId(lhEmployerId)
        if (employer == null) {
            logger.warn { "cant process payments from finch because employer with id $lhEmployerId is not exist" }
            return
        }
        val token = accessTokenRepository.findOneActiveTokenByEmployerId(employer.id)
        if (token == null) {
            logger.warn { "cant process payments from finch because of no active token for employer $lhEmployerId" }
            return
        }

        finchClient.apiClient.setBearerToken(token.finchAccessToken)
        val payments = finchClient.getIndividualPayments(startDate, endDate, finchProps.apiVersion).asFlow().map {
            paymentsMapper.toPaymentEntity(it, employer)
        }.toList()

        if (payments.isEmpty()) {
            logger.info { "no payments in finch for employer $lhEmployerId from $startDate to $endDate" }
            return
        }
        paymentsRepository.savePayments(payments)

        val requests = payments.map {
            PayStatementRequestRequests().paymentId(it.paymentId)
        }.toList()

        processPayStatements(PayStatementRequest().requests(requests))
    }

    suspend fun processPayStatements(payStatementRequest: PayStatementRequest) {
        val payStatements = finchClient.readPayStatements(finchProps.apiVersion, payStatementRequest).awaitSingle()
        val records = payStatements.responses?.flatMap {
           res ->
            res.body?.payStatements?.flatMap { body ->
                body.employeeDeductions?.filter {
                    "HEALTH SAVINGS" == it.name || "hsa_pre" == it.type || "hsa_post" == it.type
                }?.map {
                    paymentsMapper.toPayStatementEntity(res.paymentId, body.individualId, it)
                }?.toList() ?: mutableListOf()
            }?.toList() ?: mutableListOf()
        }
        if (records != null && records.isNotEmpty()) {
                payStatementsRepository.savePayStatements(records)
        } else{
            logger.info { "no pay statements with HEALTH_SAVING" }
        }

    }

    suspend fun askForFinchPaymentsByEmployer(request: FinchPaymentRequest): FinchPaymentsByEmployerResponse {
        logger.info { "askForFinchPaymentsByEmployer for employer ${request.id}" }

        val employer = employerRepository.findByLhEmployerId(request.id)

        if(employer != null) {
            val fromDb = paymentsRepository.findAllByEmployerIdPageable(employer.id)
            val from = (request.pageable.pageNumber*request.pageable.pageSize)
            val to = Math.min(fromDb.size, from.plus(request.pageable.pageSize))
            val payments = fromDb.subList(from, to)
            return if (payments.isNotEmpty()) {
                logger.info { "payments size = ${payments.size} for employer ${request.id}, from $from element to $to element" }
                val result = payments.map {
                    paymentsMapper.toPaymentProto(it)
                }.toList()
                val page = getPageable(request.pageable.pageNumber, request.pageable.pageSize, fromDb.size)
                FinchPaymentsByEmployerResponse.newBuilder().addAllPayments(result).setPageable(page).build()
            } else {
                logger.info { "no payments for employer ${request.id}" }
                FinchPaymentsByEmployerResponse.newBuilder().build()
            }
        } else {
            val id = request.id
            logger.warn { "cant process askForFinchPaymentsByEmployer because employer with id $id is not exist" }
            return FinchPaymentsByEmployerResponse.newBuilder().build()
        }
    }

    suspend fun askForFinchPayStatementsByPayment(request: FinchPayStatementRequest): FinchPayStatementsResponse {
        logger.info { "askForFinchPayStatementsByPayment for payment ${request.id}" }
        val fromDb =  payStatementsRepository.findAllByPaymentIdPageable(request.id)
        return getPayStatementsPageable(request, fromDb)
    }

    suspend fun askForFinchPayStatementsByEmployee(request: FinchPayStatementRequest): FinchPayStatementsResponse {
        logger.info { "askForFinchPayStatementsByEmployee for employee ${request.id}" }
        val fromDb = payStatementsRepository.findAllByEmployeeIdPageable(request.id)
        return getPayStatementsPageable(request, fromDb)
    }

    suspend fun getPayStatementsPageable(request: FinchPayStatementRequest, fromDb: List<Record5<Long, Long, String, LocalDate, Long>>): FinchPayStatementsResponse{
        val from = (request.pageable.pageNumber*request.pageable.pageSize)
        val to = Math.min(fromDb.size, from.plus(request.pageable.pageSize))
        val payStatements = fromDb.subList(from, to)
        return if (payStatements.isNotEmpty()) {
            logger.info { "payStatements size = ${payStatements.size} for employee ${request.id}, from $from element to $to element" }
            val result = payStatements.map {
                paymentsMapper.toPayStatementProto(it)
            }.toList()
            val page = getPageable(request.pageable.pageNumber, request.pageable.pageSize, fromDb.size)
            FinchPayStatementsResponse.newBuilder().addAllPayStatement(result).setPageable(page).build()
        } else {
            logger.info { "no payStatements for employee ${request.id}" }
            FinchPayStatementsResponse.newBuilder().build()
        }
    }

    suspend fun askForFinchPayStatementsByPaymentIds(request: FinchPayStatementByPaymentsRequest): FinchPayStatementsResponse {
        logger.info { "askForFinchPayStatementsByPaymentIds ${request.paymentIdList}" }
        val payStatements = payStatementsRepository.findAllPayStatementsByPaymentIds(request.paymentIdList)
        return if (payStatements.isNotEmpty()) {
            logger.info { "payStatements size = ${payStatements.size}" }
            val result = payStatements.map {
                paymentsMapper.toPayStatementProto(it)
            }.toList()
            FinchPayStatementsResponse.newBuilder().addAllPayStatement(result).build()
        } else {
            logger.info { "no payStatements for payments ${request.paymentIdList}" }
            FinchPayStatementsResponse.newBuilder().build()
        }
    }

    suspend fun getPageable(pageNumber: Int, pageSize: Int, totalSize: Int): Pageable {
        return Pageable.newBuilder()
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .setTotalPages(Math.ceil(totalSize.toDouble()/pageSize).toInt())
            .setTotalElements(totalSize.toLong())
            .build()
    }
}