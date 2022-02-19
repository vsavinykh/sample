package com.lanehealth.payrollservice.mapper

import com.google.protobuf.Timestamp
import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import com.lanehealth.payroll.model.generated.tables.records.FinchEmployeeRecord
import com.lanehealth.payroll.model.generated.tables.records.LhEmployeeRecord
import com.lanehealth.payrollservice.finch.contract.LhEmployee
import com.lanehealth.payrollservice.finch.contract.models.IndividualResponseResponses
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface EmployeeMapper {

    @Mappings(
        Mapping(target = "ssn", source = "source.body.ssn"),
        Mapping(target = "firstName", source = "source.body.firstName"),
        Mapping(target = "middleName", source = "source.body.middleName"),
        Mapping(target = "lastName", source = "source.body.lastName"),
        Mapping(target = "dateOfBirthday", source = "source.body.dob"),
        Mapping(target = "individualId", source = "source.individualId")
    )
    fun toFinchEntity(source: IndividualResponseResponses?): FinchEmployeeRecord

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "laneEmployeeId", source = "source.laneEmployeeId"),
        Mapping(target = "ssn", source = "source.employeeSsn"),
        Mapping(target = "firstName", source = "source.firstName"),
        Mapping(target = "middleName", source = "source.middleName"),
        Mapping(target = "lastName", source = "source.lastName"),
        Mapping(target = "dateOfBirthday", source = "source.dateOfBirthday"),
        Mapping(target = "division", source = "source.division"),
        Mapping(target = "employerId", source = "employer.id"),
        Mapping(target = "payrollId", source = "source.payrollId")
    )
    fun toLHEntity(source: LhEmployee, employer: EmployerRecord): LhEmployeeRecord

    fun toLocalDate(timestamp: Timestamp) = LocalDate.ofInstant(Instant.ofEpochSecond(timestamp.seconds), ZoneId.of("America/Chicago"))
}