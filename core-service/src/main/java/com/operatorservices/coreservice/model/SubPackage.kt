package com.operatorservices.coreservice.model

import jakarta.persistence.*

@Entity
data class SubPackage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    var name: String,
    var packageType: PackageType = PackageType.COMBO,
    var duration: Long,
    var purchasable: Boolean = true,

    @OneToMany(mappedBy = "subPackage", cascade = [CascadeType.ALL])
    val purchases: Set<Purchase> = HashSet()
) {
    constructor(name: String, packageType: PackageType, duration: Long, isPurchasable: Boolean) : this(
        null,
        name = name,
        packageType = packageType,
        duration = duration,
        purchasable = isPurchasable,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubPackage

        if (id != other.id) return false
        if (name != other.name) return false
        if (packageType != other.packageType) return false
        if (duration != other.duration) return false
        if (purchasable != other.purchasable) return false
        if (purchases != other.purchases) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + packageType.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + purchasable.hashCode()
        return result
    }

}

enum class PackageType{
    COMBO,
    CALL,
    INTERNET,
    SOCIAL
}

