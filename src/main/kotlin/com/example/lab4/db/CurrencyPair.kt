package com.example.lab4.db

import jakarta.persistence.*


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["baseCurrency", "quoteCurrency"])])
data class CurrencyPair(
    @Column(nullable = false)
    var baseCurrency: String,
    @Column(nullable = false)
    var quoteCurrency: String,
    @Column(nullable = false)
    var rate: Int,
    @Column(nullable = false)
    var factor: Int = 100
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    init {
        baseCurrency = baseCurrency.uppercase()
        quoteCurrency = quoteCurrency.uppercase()
    }

    override fun toString(): String {
        return "$baseCurrency/$quoteCurrency"
    }
}
