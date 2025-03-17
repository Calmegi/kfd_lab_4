package com.example.lab4.controllers

import com.example.lab4.db.Balance
import com.example.lab4.repositories.BalanceRepository
import com.example.lab4.repositories.UserRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/balance")
class BalanceController(
    private val userRepository: UserRepository,
    private val balanceRepository: BalanceRepository
) {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getBalances() = balanceRepository.findAll()

    @PreAuthorize("authentication.name == #username || hasRole('ADMIN')")
    @GetMapping("/{username}")
    fun getBalances(@PathVariable username: String): ResponseEntity<List<Balance>> {
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user.balances)
    }

    @PostMapping(path = ["/{username}"], consumes = ["application/json"])
    @PreAuthorize("hasRole('ADMIN')")
    fun updateBalance(
        @PathVariable username: String,
        @RequestBody @Valid balances: List<BalanceRequest>
    ): ResponseEntity<List<Balance>> {
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.notFound().build()

        for (balance in balances) {
            val userBalance = user.balances.firstOrNull { it.currencyCode == balance.currencyCode.uppercase() }
            if (userBalance == null) {
                user.balances.add(Balance(balance.currencyCode, balance.amount, user))
            } else {
                userBalance.amount += balance.amount
            }
        }
        userRepository.save(user)
        return ResponseEntity.ok(user.balances)
    }
}

data class BalanceRequest(
    @field:NotBlank
    val currencyCode: String,
    @field:PositiveOrZero
    val amount: Int
)
