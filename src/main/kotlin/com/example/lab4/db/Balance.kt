package com.example.lab4.db

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

/*
Сущность “Balance” представляет баланс пользователя.
Каждая запись связывает определённую валюту с количеством (в минимальных единицах)
и принадлежит пользователю (ExchangerUser).
*/

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "currencyCode"])])
data class Balance(
    @Column(nullable = false)
    var currencyCode: String, // Например, "RUB", "USD"
    @Column(nullable = false)
    var amount: Int, // Сумма в минимальных единицах (копейки)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    var user: ExchangerUser
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    init {
        // Приведение к верхнему регистру для единообразия.
        this.currencyCode = currencyCode.uppercase()
    }
}
