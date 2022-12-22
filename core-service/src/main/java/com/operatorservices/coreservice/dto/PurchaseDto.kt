package com.operatorservices.coreservice.dto


import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDateTime

@Relation(itemRelation = "purchase", collectionRelation = "purchases")
data class PurchaseDto(

    val id: String,
    val purchaseDate: LocalDateTime,
    val subPackage: PackageDto
) : RepresentationModel<PurchaseDto>()
