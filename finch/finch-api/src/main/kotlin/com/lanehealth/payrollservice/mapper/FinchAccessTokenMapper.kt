package com.lanehealth.payrollservice.mapper

import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import com.lanehealth.payroll.model.generated.tables.records.FinchAccessTokenRecord
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import java.time.LocalDateTime
import java.time.ZoneId

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface FinchAccessTokenMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "employerId", source = "employer.id"),
        Mapping(target = "finchAccessToken", source = "accessToken"),
        Mapping(target = "createdAt", expression = "java(now())"),
        Mapping(target = "updatedAt", expression = "java(now())"),
        Mapping(target = "isActive", constant = "true")
    )
    fun toFinchAccessTokenEntity(employer: EmployerRecord, accessToken: String): FinchAccessTokenRecord

    fun now() = LocalDateTime.now(ZoneId.of("UTC"))
}