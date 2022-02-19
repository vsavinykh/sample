package com.lanehealth.payrollservice.mapper

import com.lanehealth.payroll.model.generated.tables.records.EmployerRecord
import com.lanehealth.payroll.model.generated.tables.records.PayStatementsRecord
import com.lanehealth.payroll.model.generated.tables.records.PaymentsRecord
import com.lanehealth.payrollservice.finch.contract.models.PayStatementsBodyEmployeeDeductions
import com.lanehealth.payrollservice.finch.contract.models.Payment
import com.lanehealth.payrollservice.util.MoneyUtil.fromCentsToDollars
import org.jooq.Record5
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface PaymentsMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "paymentId", source = "source.id"),
        Mapping(target = "employerId", source = "employer.id"),
        Mapping(target = "startDate", source = "source.payPeriod.startDate"),
        Mapping(target = "endDate", source = "source.payPeriod.endDate"),
        Mapping(target = "payDate", source = "source.payDate"),
        Mapping(target = "debitDate", source = "source.debitDate")
    )
    fun toPaymentEntity(source: Payment, employer: EmployerRecord): PaymentsRecord

    @Mappings(
        Mapping(target = "paymentId", source = "paymentId"),
        Mapping(target = "individualId", source = "individualId"),
        Mapping(target = "payType", source = "source.name"),
        Mapping(target = "amount", source = "source.amount")
    )
    fun toPayStatementEntity(paymentId: String?, individualId: String?, source: PayStatementsBodyEmployeeDeductions): PayStatementsRecord


    fun toPaymentProto(source: PaymentsRecord): com.lanehealth.payrollservice.finch.contract.Payment {
        return com.lanehealth.payrollservice.finch.contract.Payment
            .newBuilder()
            .setId(source.id)
            .setPayDate(source.payDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .setDebitDate(source.debitDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .setStartDate(source.startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .setEndDate(source.endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).build()
    }

    fun toPayStatementProto(source: Record5<Long, Long, String, LocalDate, Long>): com.lanehealth.payrollservice.finch.contract.PayStatement {
        return com.lanehealth.payrollservice.finch.contract.PayStatement
            .newBuilder()
            .setPaymentId(source.get(0) as Long)
            .setPayDate((source.get(3) as LocalDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .setPayType(source.get(2) as String)
            .setLaneEmployeeId(source.get(4) as Long)
            .setAmount((source.get(1) as Long).fromCentsToDollars()).build()
    }
}