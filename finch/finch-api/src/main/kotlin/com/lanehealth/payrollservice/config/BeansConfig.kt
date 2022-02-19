package com.lanehealth.payrollservice.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.openapitools.jackson.nullable.JsonNullableModule
import org.springframework.context.support.beans


val additionalBeans =
    beans {
        bean(isPrimary = true) {
            ObjectMapper().apply {
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                registerModule(JsonNullableModule())
                setSerializationInclusion(JsonInclude.Include.ALWAYS)
            }
        }
    }