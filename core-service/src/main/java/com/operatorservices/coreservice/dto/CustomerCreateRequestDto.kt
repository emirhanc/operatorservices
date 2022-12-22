package com.operatorservices.coreservice.dto

import javax.validation.constraints.*


data class CustomerCreateRequestDto(

    @field: NotBlank
    val name: String,

    @field: NotBlank
    val surname: String,

    @field: Email   //it should be also checked if the address already exist!
    @field: NotBlank
    val email: String,

    @field: Size(min = 8, max = 16)    // it should also include mixed letters, numbers & symbols!
    val password: String,
)
