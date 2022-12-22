package com.operatorservices.coreservice.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class CustomerUpdateRequestDto(

    @field: Email   //it should be also checked if the address is new!
    @field: NotBlank
    var email: String,

    @field: Size(min = 8, max = 16)    // it should be also checked if the password is new!
    var password: String
)
