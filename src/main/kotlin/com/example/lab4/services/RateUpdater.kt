package com.example.lab4.services

import com.example.lab4.db.CurrencyPair
import com.example.lab4.repositories.CurrencyPairRepository
import org.springframework.stereotype.Service
import kotlin.math.ceil
import kotlin.random.Random

@Service
class RateUpdater(private val currencyPairRepository: CurrencyPairRepository) {
    private val changePercent = 0.05 
    
    fun updateRates(): List<CurrencyPair> {
        val pairs = currencyPairRepository.findAll()
        pairs.forEach { pair ->
            val minRate = ceil(pair.rate * (1 - changePercent)).toInt()
            val maxRate = ceil(pair.rate * (1 + changePercent)).toInt()
            // Генерируем новый курс в диапазоне [minRate, maxRate)
            pair.rate = Random.nextInt(minRate, maxRate.coerceAtLeast(minRate + 1))
        }
        return currencyPairRepository.saveAll(pairs)
    }
}
