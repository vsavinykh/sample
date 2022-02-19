package com.lanehealth.payrollservice.mapper

import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface EmployerMapper {

    @Mappings(Mapping(target = "lhEmployerId", source = "employerId"))
    fun toEmployer(employerId: String): EmployerRecord

}