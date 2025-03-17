package com.example.lab4.repositories

import com.example.lab4.db.ExchangerBalance
import org.springframework.data.jpa.repository.JpaRepository

interface ExchangerBalanceRepository : JpaRepository<ExchangerBalance, Long> {
    fun findByCurrencyCode(currencyCode: String): ExchangerBalance?
}
