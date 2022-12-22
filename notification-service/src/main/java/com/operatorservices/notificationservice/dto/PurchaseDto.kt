package com.operatorservices.notificationservice.dto

import java.time.LocalDateTime

data class PurchaseDto(
    val id: String,
    val purchaseDate: LocalDateTime,
    val subPackage: Any
)
