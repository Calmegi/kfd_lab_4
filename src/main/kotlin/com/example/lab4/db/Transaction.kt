package com.example.lab4.db

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import jakarta.persistence.*
import java.time.LocalDateTime

/*
Сущность Transaction хранит данные о проведённом обмене валют.
Сюда записываются: пользователь, строковое представление валютной пары, курс на момент обмена,
изменения балансов (отрицательное значение — списание, положительное — зачисление) и временная метка.
*/

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
data class Transaction(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    var user: ExchangerUser,
    var currencyPair: String,
    var rate: Int,
    var fromDelta: Int,
    var toDelta: Int,
    var timestamp: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
