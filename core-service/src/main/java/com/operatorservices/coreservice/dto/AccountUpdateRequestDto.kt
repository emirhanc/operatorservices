package com.operatorservices.coreservice.dto

import com.operatorservices.coreservice.model.TariffType
import java.math.BigDecimal
import jakarta.validation.constraints.Min

data class AccountUpdateRequestDto(

    @field: Min(0, message = "Account Balance can not be negative")
    val accountBalance: BigDecimal = BigDecimal.ZERO,

    val tariffType: TariffType = TariffType.STANDARD
    )
