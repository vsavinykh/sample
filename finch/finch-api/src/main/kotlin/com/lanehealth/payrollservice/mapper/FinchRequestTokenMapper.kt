package com.lanehealth.payrollservice.mapper

import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import com.lanehealth.payroll.model.generated.tables.records.FinchRequestTokenRecord
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import java.time.LocalDateTime
import java.time.ZoneId

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface FinchRequestTokenMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "employerId", source = "employer.id"),
        Mapping(target = "issuerId", source = "issuerId"),
        Mapping(target = "requestToken", source = "requestToken"),
        Mapping(target = "createdAt", expression = "java(now())"),
        Mapping(target = "updatedAt", expression = "java(now())"),
        Mapping(target = "expiresOn", expression = "java(expire())"),
        Mapping(target = "status", source = "status")
    )
    fun toFinchRequestTokenEntity(employer: EmployerRecord, issuerId: String?, requestToken: String?, status: String?): FinchRequestTokenRecord

    fun now() = LocalDateTime.now(ZoneId.of("UTC"))

    fun expire() = now().plusDays(14)
}