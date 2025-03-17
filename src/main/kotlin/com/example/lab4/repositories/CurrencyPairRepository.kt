package com.example.lab4.repositories

import com.example.lab4.db.CurrencyPair
import org.springframework.data.jpa.repository.JpaRepository

interface CurrencyPairRepository : JpaRepository<CurrencyPair, Long> {
    fun findByBaseCurrencyAndQuoteCurrency(baseCurrency: String, quoteCurrency: String): CurrencyPair?
}
