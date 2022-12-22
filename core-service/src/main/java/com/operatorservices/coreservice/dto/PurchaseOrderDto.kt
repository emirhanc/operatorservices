package com.operatorservices.coreservice.dto

data class PurchaseOrderDto(
    val accountId: String,
    val subPackageId: Long,
    val packagePrice: Short
)
