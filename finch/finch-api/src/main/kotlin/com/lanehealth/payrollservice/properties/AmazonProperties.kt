package com.lanehealth.payrollservice.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("amazon")
data class AmazonProperties(
    val awsRegion: String,
    val localAwsProfile: String? = null
)