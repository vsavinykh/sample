package com.lanehealth.payrollservice.service

import com.lanehealth.payroll.model.generated.tables.LhEmployee.LH_EMPLOYEE
import com.lanehealth.payroll.model.generated.tables.records.LhEmployeeRecord
import com.lanehealth.payrollservice.clients.FinchClient
import com.lanehealth.payrollservice.finch.contract.LhEmployees
import com.lanehealth.payrollservice.finch.contract.models.IndividualRequest
import com.lanehealth.payrollservice.finch.contract.models.IndividualRequestOptions
import com.lanehealth.payrollservice.finch.contract.models.IndividualRequestRequests
import com.lanehealth.payrollservice.mapper.EmployeeMapper
import com.lanehealth.payrollservice.properties.FinchProperties
import com.lanehealth.payrollservice.repository.EmployerRepository
import com.lanehealth.payrollservice.repository.FinchAccessTokenRepository
import com.lanehealth.payrollservice.repository.FinchEmployeeRepository
import com.lanehealth.payrollservice.repository.LHEmployeeRepository
import kotlinx.coroutines.reactive.awaitSingle
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class EmployeeFinchSynchronizationService(
    val finchClient: FinchClient,
    val finchProperties: FinchProperties,
    val lhEmployeeRepository: LHEmployeeRepository,
    val finchEmployeeRepository: FinchEmployeeRepository,
    val tokenRepository: FinchAccessTokenRepository,
    val employeeMapper: EmployeeMapper,
    val employerRepository: EmployerRepository,
) {

    suspend fun processImportEmployee(message: String) {
        logger.info { "triggered process import employee" }
        val employees = LhEmployees.newBuilder().mergeFrom(Base64.getDecoder().decode(message)).build()
        val dbRecords = employees.employeeList.mapNotNull { lhEmployee ->
            employerRepository.findByLhEmployerId(lhEmployee.laneEmployerId)?.let {
                employeeMapper.toLHEntity(lhEmployee, it)
            }
        }
        lhEmployeeRepository.insert(dbRecords)
        employees.employeeList.map {
            it.laneEmployerId
        }.forEach {
            processEmployeesFromFinch(it)
        }
        updateLHEmployees()
    }

    suspend fun processEmployeesFromFinch(lhEmployerId: String) {
        val employer = employerRepository.findByLhEmployerId(lhEmployerId)
        if (employer == null) {
            logger.warn { "cant process employees from finch because employer $lhEmployerId is not found" }
            return
        }

        val token = tokenRepository.findOneActiveTokenByEmployerId(employer.id)
        if (token == null) {
            logger.warn { "cant process employees from finch because of no active token for employer $lhEmployerId" }
            return
        }

        finchClient.apiClient.setBearerToken(token.finchAccessToken)
        val companyDirectory =
            finchClient.getCompanyDirectory(finchProperties.apiVersion, null, null).awaitSingle()
        val ids = companyDirectory.individuals?.map { IndividualRequestRequests().individualId(it.id) }?.toList() ?: listOf()
        logger.info { "count of individual ids from finch = ${ids.size}" }
        val idsForSave = removeExistedIds(ids)
        if (idsForSave.isNotEmpty()) {
            val finchEmployees = finchClient.getIndividuals(
                finchProperties.apiVersion, IndividualRequest().requests(idsForSave).options(
                    IndividualRequestOptions().addIncludeItem("ssn")
                )
            ).awaitSingle().responses?.filter {
                val body = it.body
                body?.firstName != null && body.lastName != null
            }?.map {
                employeeMapper.toFinchEntity(it)
            }?.toList() ?: listOf()
            logger.info { "count of new employees from finch = ${finchEmployees.size}" }
            finchEmployeeRepository.saveFinchEmployees(finchEmployees)
        }
    }

    suspend fun removeExistedIds(ids: List<IndividualRequestRequests>): List<IndividualRequestRequests> {
        logger.info { "input ids from finch = ${ids.map { it.individualId }}" }
        val existedIds = finchEmployeeRepository.findAllIndividualIds()
        logger.info { "existed ids = $existedIds" }
        return ids.filter { !existedIds.contains(it.individualId) }
    }

    suspend fun updateLHEmployees() {
        val inputEmployees = lhEmployeeRepository.findAllWhereFinchIdIsNull()
        val forUpdate = mutableListOf<LhEmployeeRecord>()
        for (r in inputEmployees) {
            val finchEmployee = finchEmployeeRepository.findOneByFieldsOrSSn(r.firstName, r.lastName, r.dateOfBirthday, r.ssn)
            if (r.finchEmployeeId == null && finchEmployee != null) {
                logger.info { "update lhEmployee ${r.id} set finch employee ${finchEmployee.id}" }
                r.set(LH_EMPLOYEE.FINCH_EMPLOYEE_ID, finchEmployee.id)
                forUpdate.add(r)
            }
        }
        lhEmployeeRepository.batchUpdate(forUpdate)
    }
}