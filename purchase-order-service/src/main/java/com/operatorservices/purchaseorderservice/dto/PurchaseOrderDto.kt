package com.operatorservices.purchaseorderservice.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class PurchaseOrderDto(

    @field: NotBlank
    val accountId: String,

    @field: Min(1)
    val subPackageId: Long,

    @field: Min(0)
    val packagePrice: Short
)
