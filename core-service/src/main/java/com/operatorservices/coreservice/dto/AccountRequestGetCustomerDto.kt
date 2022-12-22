package com.operatorservices.coreservice.dto

data class AccountRequestGetCustomerDto(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
)
