package com.lanehealth.payrollservice.service

import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import com.lanehealth.payroll.model.generated.tables.records.LhEmployeeRecord
import com.lanehealth.payrollservice.Base
import com.lanehealth.payrollservice.finch.contract.DeductionEmployeeItem
import com.lanehealth.payrollservice.finch.contract.DeductionItemByPayrollId
import com.lanehealth.payrollservice.finch.contract.models.InlineResponse200
import com.lanehealth.payrollservice.util.DateUtil
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono

class DeductionServiceTest: Base()  {

    @Autowired
    lateinit var deductionsService: DeductionsService

    lateinit var employer: EmployerRecord
    lateinit var laneEmployee1Ok: LhEmployeeRecord
    lateinit var laneEmployee2Ok: LhEmployeeRecord
    lateinit var laneEmployee1Zero: LhEmployeeRecord
    lateinit var laneEmployee2Zero: LhEmployeeRecord

    @BeforeEach
    fun beforeAll() = runBlocking {
        employer = testDataHelper.createRandomEmployer()
        testDataHelper.createRandomAccessToken(employer.id)

        val finchEmployee1 = testDataHelper.createRandomFinchEmployee()
        laneEmployee1Ok = testDataHelper.createRandomLHEmployee(finchEmployee1.id, employer.id)

        val finchEmployee2 = testDataHelper.createRandomFinchEmployee()
        laneEmployee2Ok = testDataHelper.createRandomLHEmployee(finchEmployee2.id, employer.id)

        val finchEmployee3 = testDataHelper.createRandomFinchEmployee()
        laneEmployee1Zero = testDataHelper.createRandomLHEmployeeWithPayroll(finchEmployee3.id, employer.id, laneEmployee1Ok.payrollId)

        val finchEmployee4 = testDataHelper.createRandomFinchEmployee()
        laneEmployee2Zero = testDataHelper.createRandomLHEmployeeWithPayroll(finchEmployee4.id, employer.id, laneEmployee2Ok.payrollId)
    }

    @Test
    fun shouldProcessDeductions() = runBlocking<Unit> {
        // init

        val date = DateUtil.formatSql(DateUtil.nowDateUsCentral())
        val requestFirstPayroll = DeductionEmployeeItem.newBuilder().setLaneEmployeeId(laneEmployee1Ok.laneEmployeeId).setAmount(1000).build()
        val requestSecondPayroll = DeductionEmployeeItem.newBuilder().setLaneEmployeeId(laneEmployee2Ok.laneEmployeeId).setAmount(2000).build()
        val request1 = DeductionItemByPayrollId.newBuilder().setPayrollId(laneEmployee1Ok.payrollId).setPayDate(date).addAllDeductionEmployeeItem(listOf(requestFirstPayroll)).build()
        val request2 = DeductionItemByPayrollId.newBuilder().setPayrollId(laneEmployee2Ok.payrollId).setPayDate(date).addAllDeductionEmployeeItem(listOf(requestSecondPayroll)).build()

        every { finchClient.createIndividualDeduction(any(), any()) } returns Mono.just(InlineResponse200().status("success"))

        // when
        deductionsService.processDeductions(employer.lhEmployerId, listOf(request1, request2))

        // then
        val result = deductionsRepository.findAll()
        assertThat(result).isNotNull.size().isEqualTo(4)

        val deduction1 = result[1]
        assertThat(deduction1.deductionId).isEqualTo(employer.deductionId)
        assertThat(deduction1.lhEmployeeId).isEqualTo(laneEmployee1Ok.laneEmployeeId)
        assertThat(deduction1.amount).isEqualTo(1000)
        assertThat(deduction1.isSentToFinch).isTrue

        val deduction2 = result[0]
        assertThat(deduction2.deductionId).isEqualTo(employer.deductionId)
        assertThat(deduction2.lhEmployeeId).isEqualTo(laneEmployee1Zero.laneEmployeeId)
        assertThat(deduction2.amount).isEqualTo(0)
        assertThat(deduction2.isSentToFinch).isTrue

        val deduction3 = result[3]
        assertThat(deduction3.deductionId).isEqualTo(employer.deductionId)
        assertThat(deduction3.lhEmployeeId).isEqualTo(laneEmployee2Ok.laneEmployeeId)
        assertThat(deduction3.amount).isEqualTo(2000)
        assertThat(deduction3.isSentToFinch).isTrue

        val deduction4 = result[2]
        assertThat(deduction4.deductionId).isEqualTo(employer.deductionId)
        assertThat(deduction4.lhEmployeeId).isEqualTo(laneEmployee2Zero.laneEmployeeId)
        assertThat(deduction4.amount).isEqualTo(0)
        assertThat(deduction4.isSentToFinch).isTrue
    }
}