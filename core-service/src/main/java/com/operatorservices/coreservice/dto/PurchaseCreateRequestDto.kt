package com.operatorservices.coreservice.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class PurchaseCreateRequestDto(

    @field: NotBlank
    val accountId: String,

    @field: Min(1)
    val subPackageId: Long,

    @field: Min(0)
    val packagePrice: Short
)