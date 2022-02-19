package com.lanehealth.payrollservice.util

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyUtil {

    fun Long.fromCentsToDollars(): Double {
        val x: Double = this.div(100.0)
        var bd: BigDecimal = BigDecimal.valueOf(x)
        bd = bd.setScale(2, RoundingMode.HALF_UP)
        return bd.toDouble()
    }
}