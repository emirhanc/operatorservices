package com.operatorservices.coreservice.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
data class Customer(


    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String = "",

    val creationDate: LocalDateTime,
    val name: String,
    val surname: String,
    var email: String,
    var password: String,

    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL])
    val accounts: Set<Account> = HashSet(),

    ) {
    constructor(
        creationDate: LocalDateTime,
        name: String,
        surname: String,
        email: String,
        password: String
    ) : this(
        "",
        creationDate = creationDate,
        name = name,
        surname = surname,
        email = email,
        password = password
    )

        override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Customer

        if (id != other.id) return false
        if (creationDate != other.creationDate) return false
        if (name != other.name) return false
        if (surname != other.surname) return false
        if (email != other.email) return false
        if (password != other.password) return false
        if (accounts != other.accounts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + surname.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}
