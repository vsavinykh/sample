package com.lanehealth.payrollservice.service

import com.lanehealth.payroll.model.generated.tables.records.FinchEmployeeRecord
import com.lanehealth.payrollservice.Base
import com.lanehealth.payrollservice.finch.contract.FinchPayStatementByPaymentsRequest
import com.lanehealth.payrollservice.finch.contract.FinchPayStatementRequest
import com.lanehealth.payrollservice.finch.contract.FinchPaymentRequest
import com.lanehealth.payrollservice.finch.contract.Pageable
import com.lanehealth.payrollservice.finch.contract.models.*
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

class PaymentServiceTest : Base() {

    @Autowired
    lateinit var paymentsService: PaymentsService

    val NOW: LocalDate = LocalDate.now()
    var payment1Id: Long = 0
    var lhEmployerId: String = ""
    var lhEmployeeId: Long = 0
    lateinit var finchEmployee: FinchEmployeeRecord

    @BeforeEach
    fun beforeAll() = runBlocking {
        val employer = testDataHelper.createRandomEmployer()
        finchEmployee = testDataHelper.createRandomFinchEmployee()
        val payment1 = testDataHelper.createPayment(employer.id, NOW, NOW, NOW)
        testDataHelper.createRandomAccessToken(employer.id)
        payment1Id = payment1.id
        lhEmployerId = employer.lhEmployerId
        val lhEmployee = testDataHelper.createRandomLHEmployee(finchEmployee.id, employer.id)
        lhEmployeeId = lhEmployee.id
        testDataHelper.createPayStatement(payment1.paymentId, finchEmployee.individualId, 1234)
    }

    @Test
    fun shouldReturnPaymentsByEmployer() = runBlocking<Unit> {
        // init
        val page = Pageable.newBuilder().setPageNumber(0).setPageSize(10).build()
        val request = FinchPaymentRequest.newBuilder().setId(lhEmployerId).setPageable(page).build()

        // when
        val result = paymentsService.askForFinchPaymentsByEmployer(request)

        // then
        assertThat(result.paymentsList).isNotNull
        assertThat(result.pageable.totalElements).isEqualTo(1)
        assertThat(result.pageable.totalPages).isEqualTo(1)
        val payment = result.paymentsList[0]
        assertThat(payment).isNotNull
        assertThat(payment.payDate).isEqualTo(NOW.toString())
    }

    @Test
    fun shouldReturnPayStatementByPaymentId() = runBlocking<Unit> {
        // init
        val page = Pageable.newBuilder().setPageNumber(0).setPageSize(10).build()
        val request = FinchPayStatementRequest.newBuilder().setId(payment1Id).setPageable(page).build()

        // when
        val result = paymentsService.askForFinchPayStatementsByPayment(request)

        // then
        assertThat(result.payStatementList).isNotNull
        assertThat(result.pageable.totalElements).isEqualTo(1)
        assertThat(result.pageable.totalPages).isEqualTo(1)
        val payStatement = result.payStatementList[0]
        assertThat(payStatement).isNotNull
        assertThat(payStatement.amount).isEqualTo(12.34)
        assertThat(payStatement.payDate).isEqualTo(NOW.toString())
    }

    @Test
    fun shouldReturnPayStatementsByEmployee() = runBlocking<Unit> {
        // init
        val page = Pageable.newBuilder().setPageNumber(0).setPageSize(10).build()
        val request = FinchPayStatementRequest.newBuilder().setId(lhEmployeeId).setPageable(page).build()

        // when
        val result = paymentsService.askForFinchPayStatementsByEmployee(request)

        // then
        assertThat(result.payStatementList).isNotNull
        assertThat(result.pageable.totalElements).isEqualTo(1)
        assertThat(result.pageable.totalPages).isEqualTo(1)
        val payStatement = result.payStatementList[0]
        assertThat(payStatement).isNotNull
        assertThat(payStatement.amount).isEqualTo(12.34)
        assertThat(payStatement.payDate).isEqualTo(NOW.toString())
    }

    @Test
    fun shouldReturnPayStatementsByPaymentIds() = runBlocking<Unit> {
        // init
        val request = FinchPayStatementByPaymentsRequest.newBuilder().addAllPaymentId(listOf(payment1Id)).build()

        // when
        val result = paymentsService.askForFinchPayStatementsByPaymentIds(request)

        // then
        assertThat(result.payStatementList).isNotNull
        val payStatement = result.payStatementList[0]
        assertThat(payStatement).isNotNull
        assertThat(payStatement.amount).isEqualTo(12.34)
        assertThat(payStatement.payDate).isEqualTo(NOW.toString())
    }

    @Test
    fun shouldProcessPayment() = runBlocking<Unit> {
        // before
        testDataHelper.deleteFromPaymentsAndPayStatements()
        val payment1 = Payment()
        payment1.id = "payment1id"
        val date1 = NOW.minusMonths(1)
        payment1.payDate = date1.toString()
        payment1.payPeriod?.startDate = date1.toString()
        payment1.payPeriod?.endDate = date1.toString()

        val payment2 = Payment()
        payment2.id = "payment2id"
        val date2 = NOW.minusWeeks(1)
        payment2.payDate = date2.toString()
        payment2.payPeriod?.startDate = date2.toString()
        payment2.payPeriod?.endDate = date2.toString()

        val payStatement = PayStatements()
        val payStatement1 = PayStatementsResponses()
        payStatement1.paymentId = payment1.id
        val body1 = PayStatementsBody()
        val pay1 = PayStatementsBodyPayStatements()
        pay1.individualId = finchEmployee.individualId
        val deductions1 = PayStatementsBodyEmployeeDeductions().apply {
            amount = 1234
            type = "hsa_post"
        }
        pay1.employeeDeductions = listOf(deductions1)
        body1.addPayStatementsItem(pay1)
        payStatement1.body = body1
        payStatement.addResponsesItem(payStatement1)

        val payStatement2 = PayStatementsResponses()
        payStatement2.paymentId = payment2.id
        val body2 = PayStatementsBody()
        val pay2 = PayStatementsBodyPayStatements()
        pay2.individualId = finchEmployee.individualId
        val deductions2 = PayStatementsBodyEmployeeDeductions().apply {
            amount = 2234
            type = "hsa_pre"
        }
        pay2.employeeDeductions = listOf(deductions2)
        body2.addPayStatementsItem(pay2)
        payStatement2.body = body2
        payStatement.addResponsesItem(payStatement2)

        every { finchClient.getIndividualPayments(any(), any(), any()) } returns Flux.just(payment1, payment2)
        every { finchClient.readPayStatements(any(), any()) } returns Mono.just(payStatement)

        // when
        paymentsService.processPayments(lhEmployerId, LocalDate.now().minusYears(1).toString(), NOW.toString())

        // then
        val resultPayments = paymentsRepository.findAll()
        assertThat(resultPayments).isNotNull.size().isEqualTo(2)
        val payment1Result = resultPayments[0]
        assertThat(payment1Result.paymentId).isEqualTo(payment1.id)
        assertThat(payment1Result.payDate).isEqualTo(date1)
        val payment2Result = resultPayments[1]
        assertThat(payment2Result.paymentId).isEqualTo(payment2.id)
        assertThat(payment2Result.payDate).isEqualTo(date2)

        val resultPayStatements = payStatementsRepository.findAll()
        assertThat(resultPayStatements).isNotNull.size().isEqualTo(2)
        val payStatement1Result = resultPayStatements[0]
        assertThat(payStatement1Result.paymentId).isEqualTo(payment1.id)
        assertThat(payStatement1Result.individualId).isEqualTo(finchEmployee.individualId)
        assertThat(payStatement1Result.amount).isEqualTo(1234)
        val payStatement2Result = resultPayStatements[1]
        assertThat(payStatement2Result.paymentId).isEqualTo(payment2.id)
        assertThat(payStatement2Result.individualId).isEqualTo(finchEmployee.individualId)
        assertThat(payStatement2Result.amount).isEqualTo(2234)
    }
}