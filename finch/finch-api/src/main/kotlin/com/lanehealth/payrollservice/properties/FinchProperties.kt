package com.lanehealth.payrollservice.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("finch")
class FinchProperties(
    val clientId: String,
    val clientSecret: String,
    val finchUrl: String,
    val finchAuthUrl: String,
    val apiVersion: String,
    val isSandbox: Boolean
)