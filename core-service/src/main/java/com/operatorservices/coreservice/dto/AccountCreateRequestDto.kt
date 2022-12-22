package com.operatorservices.coreservice.dto

import com.operatorservices.coreservice.model.TariffType
import java.math.BigDecimal
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank


data class  AccountCreateRequestDto(

    @field: NotBlank
    val customerId: String,

    @field: Min(0)
    val accountBalance: BigDecimal = BigDecimal.ZERO,

    val tariffType: TariffType = TariffType.STANDARD
)
