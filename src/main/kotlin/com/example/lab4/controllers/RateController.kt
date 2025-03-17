package com.example.lab4.controllers

import com.example.lab4.db.CurrencyPair
import com.example.lab4.repositories.CurrencyPairRepository
import com.example.lab4.services.RateUpdater
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI


@RestController
@RequestMapping("/api/rates")
class RateController(
    private val currencyPairRepository: CurrencyPairRepository,
    private val rateUpdater: RateUpdater
) {

    @GetMapping
    fun getRates(): ResponseEntity<List<CurrencyPair>> {
        return ResponseEntity.ok(currencyPairRepository.findAll())
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun addRate(@Valid @RequestBody rateRequest: RateRequest): ResponseEntity<Any> {
        val existing = currencyPairRepository.findByBaseCurrencyAndQuoteCurrency(
            rateRequest.baseCurrency.uppercase(), rateRequest.quoteCurrency.uppercase()
        )
        if (existing != null) {
            return ResponseEntity.badRequest().body("Валютная пара уже существует.")
        }
        val currencyPair = CurrencyPair(
            baseCurrency = rateRequest.baseCurrency,
            quoteCurrency = rateRequest.quoteCurrency,
            rate = rateRequest.rate,
            factor = rateRequest.factor
        )
        val saved = currencyPairRepository.save(currencyPair)
        return ResponseEntity.created(URI("/api/rates/${saved.id}")).body(saved)
    }


    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateRates(): ResponseEntity<List<CurrencyPair>> {
        val updatedRates = rateUpdater.updateRates()
        return ResponseEntity.ok(updatedRates)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteRate(@PathVariable id: Long): ResponseEntity<Void> {
        currencyPairRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}

data class RateRequest(
    @field:NotBlank(message = "Base currency cannot be blank")
    val baseCurrency: String,
    @field:NotBlank(message = "Quote currency cannot be blank")
    val quoteCurrency: String,
    @field:Positive(message = "Rate must be positive")
    @field:NotNull(message = "Rate cannot be null")
    val rate: Int,
    @field:Positive(message = "Factor must be positive")
    val factor: Int = 100
)
