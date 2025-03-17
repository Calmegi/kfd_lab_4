package com.example.lab4.db

import jakarta.persistence.*

/*
Сущность ExchangerBalance представляет баланс самого терминала обмена.
Каждая запись содержит валюту (уникально) и количество доступных единиц.
*/

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
