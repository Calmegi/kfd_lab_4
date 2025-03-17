package com.example.lab4.db

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "currencyCode"])])
data class Balance(
    @Column(nullable = false)
    var currencyCode: String, 
    @Column(nullable = false)
    var amount: Int, 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    var user: ExchangerUser
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    init {
        this.currencyCode = currencyCode.uppercase()
    }
}
