package com.lanehealth.payrollservice.util

import com.lanehealth.payroll.model.generated.Tables.FINCH_ACCESS_TOKEN
import com.lanehealth.payroll.model.generated.Tables.PAYMENTS
import com.lanehealth.payroll.model.generated.tables.Employer.EMPLOYER
import com.lanehealth.payroll.model.generated.tables.FinchEmployee.FINCH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.LhEmployee.LH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.PayStatements.PAY_STATEMENTS
import com.lanehealth.payroll.model.generated.tables.SentDeduction.SENT_DEDUCTION
import com.lanehealth.payroll.model.generated.tables.records.*
import com.lanehealth.payrollservice.repository.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class TestDataHelper{

    @Autowired
    lateinit var dslContext: DSLContext

    @Autowired
    lateinit var paymentsRepository: PaymentsRepository

    @Autowired
    lateinit var payStatementsRepository: PayStatementsRepository

    @Autowired
    lateinit var finchEmployeeRepository: FinchEmployeeRepository

    @Autowired
    lateinit var lhEmployeeRepository: LHEmployeeRepository

    @Autowired
    lateinit var employerRepository: EmployerRepository

    @Autowired
    lateinit var accessTokenRepository: FinchAccessTokenRepository

    suspend fun createRandomAccessToken(employerId: Long) {
        val token = FinchAccessTokenRecord()
        token.finchAccessToken = RandomStringUtils.randomAlphanumeric(10)
        token.employerId = employerId
        token.isActive = true
        token.createdAt = LocalDateTime.now()
        token.updatedAt = LocalDateTime.now()
        accessTokenRepository.save(token)
    }

    suspend fun createRandomEmployer(): EmployerRecord {
        val employer = EmployerRecord()
        employer.lhEmployerId = RandomStringUtils.randomAlphanumeric(10)
        employer.deductionId = RandomStringUtils.randomAlphanumeric(10)
        employerRepository.save(employer)
        return employerRepository.findByLhEmployerId(employer.lhEmployerId)!!
    }

    suspend fun createRandomLHEmployee(finchEmployeeId: Long, employerId: Long): LhEmployeeRecord {
        return createRandomLHEmployeeWithPayroll(finchEmployeeId, employerId, Random().nextLong())
    }

    suspend fun createRandomLHEmployeeWithPayroll(finchEmployeeId: Long, employerId: Long, payrollId: Long): LhEmployeeRecord {
        val employee = LhEmployeeRecord()
        val str = RandomStringUtils.randomAlphanumeric(10)
        employee.firstName = str
        employee.middleName = str
        employee.lastName = str
        employee.dateOfBirthday = LocalDate.now()
        employee.ssn = UUID.randomUUID().toString()
        employee.laneEmployeeId = Random().nextLong()
        employee.division = UUID.randomUUID().toString()
        employee.employerId = employerId
        employee.payrollId = payrollId
        lhEmployeeRepository.insert(listOf(employee))
        dslContext.update(LH_EMPLOYEE).set(LH_EMPLOYEE.FINCH_EMPLOYEE_ID, finchEmployeeId).where(LH_EMPLOYEE.LANE_EMPLOYEE_ID.eq(employee.laneEmployeeId)).awaitSingle()
        return dslContext.selectFrom(LH_EMPLOYEE).where(LH_EMPLOYEE.LANE_EMPLOYEE_ID.eq(employee.laneEmployeeId))
            .awaitSingle()
    }

    suspend fun createRandomFinchEmployee(): FinchEmployeeRecord = coroutineScope {
        val employee = FinchEmployeeRecord()
        val str = RandomStringUtils.randomAlphanumeric(10)
        employee.firstName = str
        employee.middleName = str
        employee.lastName = str
        employee.dateOfBirthday = LocalDate.now()
        employee.ssn = UUID.randomUUID().toString()
        employee.individualId = UUID.randomUUID().toString()
        finchEmployeeRepository.saveFinchEmployees(listOf(employee))
        finchEmployeeRepository.findOneByFieldsOrSSn(str,str,employee.dateOfBirthday,employee.ssn)!!
    }

    suspend fun createPayment(employerId: Long, payDate: LocalDate, startDate: LocalDate, endDate: LocalDate): PaymentsRecord {
        val payment = PaymentsRecord()
        payment.paymentId = UUID.randomUUID().toString()
        payment.payDate = payDate
        payment.startDate = startDate
        payment.endDate = endDate
        payment.debitDate = payDate
        payment.employerId = employerId
        paymentsRepository.savePayments(listOf(payment))
        return paymentsRepository.findOneByPaymentId(payment.paymentId)!!
    }

    suspend fun createPayStatement(paymentId: String, individualId: String, amount: Long) {
        val payStatement = PayStatementsRecord()
        payStatement.paymentId = paymentId
        payStatement.individualId = individualId
        payStatement.payType = "payType"
        payStatement.amount = amount
        payStatementsRepository.savePayStatements(listOf(payStatement))
    }

    suspend fun clearAllTables(): Int {
        var deleted = dslContext.deleteFrom(SENT_DEDUCTION).awaitSingle()
        deleted += dslContext.deleteFrom(LH_EMPLOYEE).awaitSingle()
        deleted += dslContext.deleteFrom(FINCH_EMPLOYEE).awaitSingle()
        deleted += dslContext.deleteFrom(PAY_STATEMENTS).awaitSingle()
        deleted += dslContext.deleteFrom(PAYMENTS).awaitSingle()
        deleted += dslContext.deleteFrom(FINCH_ACCESS_TOKEN).awaitSingle()
        deleted += dslContext.deleteFrom(EMPLOYER).awaitSingle()
        return deleted
    }

    suspend fun deleteFromPaymentsAndPayStatements() {
        dslContext.deleteFrom(PAY_STATEMENTS).awaitSingle()
        dslContext.deleteFrom(PAYMENTS).awaitSingle()
    }
}