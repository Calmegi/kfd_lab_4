package com.example.lab4.services

import com.example.lab4.db.Balance
import com.example.lab4.db.CurrencyPair
import com.example.lab4.db.ExchangerBalance
import com.example.lab4.db.Transaction
import com.example.lab4.db.ExchangerUser
import com.example.lab4.repositories.CurrencyPairRepository
import com.example.lab4.repositories.ExchangerBalanceRepository
import com.example.lab4.repositories.TransactionRepository
import com.example.lab4.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExchangeService(
    private val userRepository: UserRepository,
    private val currencyPairRepository: CurrencyPairRepository,
    private val exchangerBalanceRepository: ExchangerBalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val rateUpdater: RateUpdater
) {

    fun getTransactionsForUser(username: String): List<Transaction> {
        return transactionRepository.findByUserUsername(username)
    }

    @Transactional
    fun exchangeCurrency(username: String, fromCurrency: String, toCurrency: String, amount: Int): ResponseEntity<Any> {
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден.")
            
        val currencyPair = currencyPairRepository.findByBaseCurrencyAndQuoteCurrency(
            fromCurrency.uppercase(), toCurrency.uppercase()
        ) ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Валютная пара $fromCurrency/$toCurrency не найдена.")

        val exchangeAmount = ((amount.toDouble() / currencyPair.rate.toDouble()) * currencyPair.factor).toInt()

        val userFromBalance = user.balances.find { it.currencyCode == fromCurrency.uppercase() }
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Баланс в валюте $fromCurrency у пользователя не найден.")
        if (userFromBalance.amount < amount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недостаточно средств у пользователя для обмена.")
        }

        val terminalToBalance: ExchangerBalance = exchangerBalanceRepository.findByCurrencyCode(toCurrency.uppercase())
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Баланс терминала для валюты $toCurrency не найден.")
        if (terminalToBalance.amount < exchangeAmount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недостаточно средств в терминале для обмена.")
        }

        var userToBalance = user.balances.find { it.currencyCode == toCurrency.uppercase() }
        if (userToBalance == null) {
            userToBalance = Balance(toCurrency.uppercase(), exchangeAmount, user)
            user.balances.add(userToBalance)
        } else {
            userToBalance.amount += exchangeAmount
        }

        var terminalFromBalance = exchangerBalanceRepository.findByCurrencyCode(fromCurrency.uppercase())
        if (terminalFromBalance == null) {
            terminalFromBalance = ExchangerBalance(fromCurrency.uppercase(), amount)
            exchangerBalanceRepository.save(terminalFromBalance)
        } else {
            terminalFromBalance.amount += amount
        }

        val transaction = Transaction(
            user = user,
            currencyPair = "${currencyPair.baseCurrency}/${currencyPair.quoteCurrency}",
            rate = currencyPair.rate,
            fromDelta = -amount,
            toDelta = exchangeAmount
        )
        transactionRepository.save(transaction)

        rateUpdater.updateRates()

        userRepository.save(user)

        return ResponseEntity.ok(transaction)
    }
}
