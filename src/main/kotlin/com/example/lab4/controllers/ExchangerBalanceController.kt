package com.example.lab4.controllers

import com.example.lab4.db.ExchangerBalance
import com.example.lab4.repositories.ExchangerBalanceRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/exchangerbalance")
class ExchangerBalanceController(
    private val exchangerBalanceRepository: ExchangerBalanceRepository
) {


    @GetMapping
    fun getExchangerBalances(): ResponseEntity<List<ExchangerBalance>> {
        val balances = exchangerBalanceRepository.findAll()
        return ResponseEntity.ok(balances)
    }


    @PostMapping(consumes = ["application/json"])
    @PreAuthorize("hasRole('ADMIN')")
    fun updateExchangerBalances(
        @RequestBody @Valid requests: List<ExchangerBalanceRequest>
    ): ResponseEntity<List<ExchangerBalance>> {
        requests.forEach { req ->
            val currency = req.currencyCode.uppercase()
            var terminalBalance = exchangerBalanceRepository.findByCurrencyCode(currency)
            if (terminalBalance == null) {
                terminalBalance = ExchangerBalance(currency, req.amount)
                exchangerBalanceRepository.save(terminalBalance)
            } else {
                terminalBalance.amount += req.amount
                exchangerBalanceRepository.save(terminalBalance)
            }
        }
        val updatedBalances = exchangerBalanceRepository.findAll()
        return ResponseEntity.ok(updatedBalances)
    }
}

data class ExchangerBalanceRequest(
    @field:NotBlank(message = "Currency code не может быть пустым")
    val currencyCode: String,
    @field:PositiveOrZero(message = "Amount должен быть положительным или равным нулю")
    val amount: Int
)
