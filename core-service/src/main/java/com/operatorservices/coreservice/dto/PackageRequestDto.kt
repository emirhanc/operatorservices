package com.operatorservices.coreservice.dto

import com.operatorservices.coreservice.model.PackageType
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class PackageRequestDto(

    @field: NotBlank
    val name: String,

    val packageType: PackageType = PackageType.COMBO,

    val purchasable: Boolean = true,

    @field: Min(0)
    val duration: Long
)
