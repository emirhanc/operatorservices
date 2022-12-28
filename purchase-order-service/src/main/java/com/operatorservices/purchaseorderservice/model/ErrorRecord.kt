package com.operatorservices.purchaseorderservice.model


import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash


@RedisHash("errorRecord")
data class ErrorRecord @JvmOverloads constructor(

    @Id
    val id: String? = null,
    val code: Short,
    val message: String

)