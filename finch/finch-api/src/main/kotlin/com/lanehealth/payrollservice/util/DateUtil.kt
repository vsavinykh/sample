package com.lanehealth.payrollservice.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtil {

    val SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun nowDateUsCentral(): LocalDate {
        return nowUsCentralZone().toLocalDate()
    }

    fun nowUsCentralZone() : LocalDateTime {
        return LocalDateTime.now(ZoneId.of("US/Central"))
    }

    fun parse(formattedDate: String?): LocalDate? {
        return if (formattedDate.isNullOrEmpty()) { null }
        else LocalDate.parse(formattedDate, SQL_DATE_TIME_FORMATTER)
    }

    fun formatSql(date: LocalDate): String = date.format(SQL_DATE_TIME_FORMATTER)
}