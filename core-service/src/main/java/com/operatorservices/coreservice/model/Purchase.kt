package com.operatorservices.coreservice.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
data class Purchase(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String,

    val purchaseDate: LocalDateTime?,
    val packagePrice: Short,

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account?,

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    val subPackage: SubPackage?


) {
    constructor(
        purchaseDate: LocalDateTime,
        account: Account,
        subPackage: SubPackage,
        packagePrice: Short,
    ) : this(
        "",
        purchaseDate = purchaseDate,
        account = account,
        subPackage = subPackage,
        packagePrice = packagePrice
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Purchase

        if (id != other.id) return false
        if (purchaseDate != other.purchaseDate) return false
        if (packagePrice != other.packagePrice) return false
        if (account != other.account) return false
        if (subPackage != other.subPackage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (purchaseDate?.hashCode() ?: 0)
        result = 31 * result + packagePrice
        result = 31 * result + (account?.hashCode() ?: 0)
        result = 31 * result + (subPackage?.hashCode() ?: 0)
        return result
    }

}

