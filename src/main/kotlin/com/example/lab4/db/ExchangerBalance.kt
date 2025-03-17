package com.example.lab4.db

import jakarta.persistence.*

@Entity
data class ExchangerBalance(
    @Column(nullable = false, unique = true)
    var currencyCode: String,
    @Column(nullable = false)
    var amount: Int
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    init {
        currencyCode = currencyCode.uppercase()
    }
}
