package com.operatorservices.coreservice.dto

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(itemRelation = "account", collectionRelation = "accounts")
data class GetAccountsByPackageDto(
    val id: String,
    val customer: AccountRequestGetCustomerDto
) : RepresentationModel<GetAccountsByPackageDto>()
