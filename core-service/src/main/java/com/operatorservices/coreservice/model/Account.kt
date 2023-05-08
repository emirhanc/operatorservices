package com.operatorservices.coreservice.model

import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
data class Account(

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String,

    @ManyToOne
    @JoinColumn(name = "customer_id",  nullable = false)
    val customer: Customer?,

    val creationDate: LocalDateTime?,

    var accountBalance: BigDecimal? = BigDecimal.ZERO,
    var tariffType: TariffType? = TariffType.STANDARD,

    @OneToMany(mappedBy = "account", cascade = [CascadeType.ALL])
    val purchases: Set<Purchase> = HashSet(),

    ) {
    constructor(
        creationDate: LocalDateTime,
        customer: Customer,
        accountBalance: BigDecimal,
        tariffType: TariffType
    ) : this(
        "",
        creationDate = creationDate,
        customer = customer,
        accountBalance = accountBalance,
        tariffType = tariffType
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (id != other.id) return false
        if (customer != other.customer) return false
        if (creationDate != other.creationDate) return false
        if (accountBalance != other.accountBalance) return false
        if (tariffType != other.tariffType) return false
        if (purchases != other.purchases) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (customer?.hashCode() ?: 0)
        result = 31 * result + (creationDate?.hashCode() ?: 0)
        result = 31 * result + (accountBalance?.hashCode() ?: 0)
        result = 31 * result + (tariffType?.hashCode() ?: 0)
        return result
    }


}
enum class TariffType {
    ECONOMY,
    STANDARD,
    PREMIUM
}