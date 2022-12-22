package com.operatorservices.purchaseorderservice.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class PurchaseOrderDto(

    @field: NotBlank
    val accountId: String,

    @field: Min(1)
    val subPackageId: Long,

    @field: Min(0)
    val packagePrice: Short
)
