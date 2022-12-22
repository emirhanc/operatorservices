package com.operatorservices.coreservice.dto

import com.operatorservices.coreservice.model.TariffType
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.math.BigDecimal
import java.time.LocalDateTime

@Relation(itemRelation = "account", collectionRelation = "accounts")
data class CustomerRequestGetAccountDto(
    val id: String,
    val creationDate: LocalDateTime,
    val tariffType: TariffType,
    val accountBalance: BigDecimal,
    val purchases: Set<PurchaseDto>
) : RepresentationModel<CustomerRequestGetAccountDto>()