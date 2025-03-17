package com.example.lab4.controllers

import com.example.lab4.db.Transaction
import com.example.lab4.services.ExchangeService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.parameters.P
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/transactions")
class TransactionsController(
    private val exchangeService: ExchangeService
) {
    @GetMapping("/user/{username}")
    @PreAuthorize("authentication.name == #username || hasRole('ADMIN')")
    fun getUserTransactions(@PathVariable @P("username") username: String): ResponseEntity<List<Transaction>> {
        val transactions = exchangeService.getTransactionsForUser(username)
        return ResponseEntity.ok(transactions)
    }

    @PostMapping("/exchange")
    @PreAuthorize("authentication.name == #exchangeRequest.username")
    fun exchangeCurrency(@Valid @RequestBody exchangeRequest: ExchangeRequest): ResponseEntity<Any> {
        return exchangeService.exchangeCurrency(
            username = exchangeRequest.username,
            fromCurrency = exchangeRequest.fromCurrency,
            toCurrency = exchangeRequest.toCurrency,
            amount = exchangeRequest.amount
        )
    }
}


data class ExchangeRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val fromCurrency: String,
    @field:NotBlank
    val toCurrency: String,
    @field:Positive(message = "Amount must be positive")
    val amount: Int
)
