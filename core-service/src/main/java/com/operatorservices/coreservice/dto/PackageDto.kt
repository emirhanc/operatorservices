package com.operatorservices.coreservice.dto

import com.operatorservices.coreservice.model.PackageType
import org.springframework.hateoas.server.core.Relation
import java.io.Serializable

@Relation(itemRelation = "package", collectionRelation = "packages")
data class PackageDto(

    val id: Long?,
    val name: String,
    val packageType: PackageType,
    val duration: Long,
    val purchasable: Boolean
): Serializable
