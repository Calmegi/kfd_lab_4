package com.example.lab4.db

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import jakarta.persistence.*

/*
Сущность ExchangerUser представляет пользователя системы обмена валют.
Содержит уникальное имя, зашифрованный пароль, список ролей (authorities),
а также связи с балансами и историей транзакций.
*/

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "username")
data class ExchangerUser(
    @Column(unique = true, nullable = false)
    var username: String,
    @Column(nullable = false)
    @JsonIgnore
    var password: String,
    @ElementCollection(fetch = FetchType.EAGER)
    var authorities: List<String>,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    var balances: MutableList<Balance> = mutableListOf(),
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    var transactions: MutableList<Transaction> = mutableListOf()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
