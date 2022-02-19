package com.lanehealth.payrollservice.service

import com.google.protobuf.Timestamp
import com.lanehealth.payrollservice.Base
import com.lanehealth.payrollservice.finch.contract.LhEmployee
import com.lanehealth.payrollservice.finch.contract.LhEmployees
import com.lanehealth.payrollservice.finch.contract.models.*
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class EmployeeFinchSynchronizationServiceTest: Base() {
    @Autowired
    lateinit var employeeFinchSynchronizationService: EmployeeFinchSynchronizationService

    val NOW: LocalDate = LocalDate.now()

    @Test
    fun shouldProcessEmployeesFromLH() = runBlocking<Unit> {
        // init
        val employer = testDataHelper.createRandomEmployer()
        testDataHelper.createRandomAccessToken(employer.id)
        val employee1 = createRandomLhEmployee(11, employer.lhEmployerId)
        val employee2 = createRandomLhEmployee(12, employer.lhEmployerId)
        val request = LhEmployees.newBuilder().addAllEmployee(listOf(employee1, employee2)).build()
        val message = Base64.getEncoder().encodeToString(request.toByteArray())

        val company = CompanyDirectory()
        val ind1 = CompanyDirectoryIndividuals()
        ind1.id = RandomStringUtils.randomAlphanumeric(10)
        val ind2 = CompanyDirectoryIndividuals()
        ind2.id = RandomStringUtils.randomAlphanumeric(10)
        company.individuals = listOf(ind1, ind2)

        val individual = IndividualResponse()
        val body1 = IndividualResponseBody().apply {
            firstName = employee1.firstName
            middleName = employee1.middleName
            lastName = employee1.lastName
            dob = NOW.toString()
            ssn = employee1.employeeSsn
        }
        val indResp1 = IndividualResponseResponses().apply {
            individualId = ind1.id
            body = body1
        }

        val body2 = IndividualResponseBody().apply {
            firstName = employee2.firstName
            middleName = employee2.middleName
            lastName = employee2.lastName
            dob = NOW.toString()
            ssn = employee2.employeeSsn
        }
        val indResp2 = IndividualResponseResponses().apply {
            individualId = ind2.id
            body = body2
        }

        individual.responses = listOf(indResp1, indResp2)

        every { finchClient.getCompanyDirectory(any(), any(), any()) } answers { Mono.just(company) }
        every { finchClient.getIndividuals(any(), any()) } returns Mono.just(individual)

        // when
        employeeFinchSynchronizationService.processImportEmployee(message)

        // then
        val finchEmployeeResult = finchEmployeeRepository.findAll()
        assertThat(finchEmployeeResult).isNotNull.size().isEqualTo(2)
        val finchEmployee1 = finchEmployeeResult[0]
        assertThat(finchEmployee1.firstName).isEqualTo(employee1.firstName)
        assertThat(finchEmployee1.middleName).isEqualTo(employee1.middleName)
        assertThat(finchEmployee1.lastName).isEqualTo(employee1.lastName)
        assertThat(finchEmployee1.ssn).isEqualTo(employee1.employeeSsn)
        assertThat(finchEmployee1.dateOfBirthday).isEqualTo(NOW)
        assertThat(finchEmployee1.individualId).isEqualTo(ind1.id)

        val finchEmployee2 = finchEmployeeResult[1]
        assertThat(finchEmployee2.firstName).isEqualTo(employee2.firstName)
        assertThat(finchEmployee2.middleName).isEqualTo(employee2.middleName)
        assertThat(finchEmployee2.lastName).isEqualTo(employee2.lastName)
        assertThat(finchEmployee2.ssn).isEqualTo(employee2.employeeSsn)
        assertThat(finchEmployee2.dateOfBirthday).isEqualTo(NOW)
        assertThat(finchEmployee2.individualId).isEqualTo(ind2.id)


        val lhEmployeeResult = lhEmployeeRepository.findAll()
        assertThat(lhEmployeeResult).isNotNull.size().isEqualTo(2)
        val lhEmployee1 = lhEmployeeResult[0]
        assertThat(lhEmployee1.firstName).isEqualTo(employee1.firstName)
        assertThat(lhEmployee1.middleName).isEqualTo(employee1.middleName)
        assertThat(lhEmployee1.lastName).isEqualTo(employee1.lastName)
        assertThat(lhEmployee1.dateOfBirthday).isEqualTo(NOW)
        assertThat(lhEmployee1.ssn).isEqualTo(employee1.employeeSsn)
        assertThat(lhEmployee1.laneEmployeeId).isEqualTo(employee1.laneEmployeeId)
        assertThat(lhEmployee1.division).isEqualTo(employee1.division)
        assertThat(lhEmployee1.finchEmployeeId).isEqualTo(finchEmployee1.id)
        assertThat(lhEmployee1.employerId).isEqualTo(employer.id)

        val lhEmployee2 = lhEmployeeResult[1]
        assertThat(lhEmployee2.firstName).isEqualTo(employee2.firstName)
        assertThat(lhEmployee2.middleName).isEqualTo(employee2.middleName)
        assertThat(lhEmployee2.lastName).isEqualTo(employee2.lastName)
        assertThat(lhEmployee2.dateOfBirthday).isEqualTo(NOW)
        assertThat(lhEmployee2.ssn).isEqualTo(employee2.employeeSsn)
        assertThat(lhEmployee2.laneEmployeeId).isEqualTo(employee2.laneEmployeeId)
        assertThat(lhEmployee2.division).isEqualTo(employee2.division)
        assertThat(lhEmployee2.finchEmployeeId).isEqualTo(finchEmployee2.id)
        assertThat(lhEmployee2.employerId).isEqualTo(employer.id)
    }

    fun createRandomLhEmployee(employeeId: Long, employerId: String): LhEmployee {
        val str = RandomStringUtils.randomAlphanumeric(10)
        val time: Long = NOW.atStartOfDay().atZone(ZoneId.of("America/Chicago")).toInstant().epochSecond
        return LhEmployee.newBuilder()
            .setFirstName(str)
            .setMiddleName(str)
            .setLastName(str)
            .setDateOfBirthday(Timestamp.newBuilder().setSeconds(time))
            .setEmployeeSsn(str)
            .setDivision(str)
            .setLaneEmployeeId(employeeId)
            .setLaneEmployerId(employerId).build()
    }
}