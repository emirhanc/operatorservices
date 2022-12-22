package com.operatorservices.coreservice.dto

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDateTime

@Relation(itemRelation = "customer", collectionRelation = "customers")
data class CustomerGetDto(

    val id: String,
    val creationDate: LocalDateTime,
    val name: String,
    val surname: String,
    val email: String,
    val accounts: Set<CustomerRequestGetAccountDto>?
) : RepresentationModel<CustomerGetDto>()
