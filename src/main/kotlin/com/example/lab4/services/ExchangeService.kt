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

/*
Сервис ExchangeService реализует бизнес-логику обмена валют.
Метод exchangeCurrency:
  1. Проверяет наличие пользователя.
  2. Ищет валютную пару по ключу "FROM/TO".
  3. Рассчитывает сумму обмена (exchangeAmount = (amount / rate) * factor).
  4. Проверяет, достаточно ли средств у пользователя и терминала.
  5. Обновляет балансы: списывает у пользователя валюту FROM, зачисляет валюту TO;
     и изменяет балансы терминала соответствующим образом.
  6. Создаёт запись транзакции.
  7. Вызывает обновление курсов.
*/

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
        // Находим пользователя
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден.")
        // Находим валютную пару по ключу "FROM/TO"
        val currencyPair = currencyPairRepository.findByBaseCurrencyAndQuoteCurrency(
            fromCurrency.uppercase(), toCurrency.uppercase()
        ) ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Валютная пара $fromCurrency/$toCurrency не найдена.")

        // Рассчитываем сумму обмена для валюты TO: exchangeAmount = (amount / rate) * factor.
        val exchangeAmount = ((amount.toDouble() / currencyPair.rate.toDouble()) * currencyPair.factor).toInt()

        // Проверяем наличие средств у пользователя на балансе валюты FROM
        val userFromBalance = user.balances.find { it.currencyCode == fromCurrency.uppercase() }
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Баланс в валюте $fromCurrency у пользователя не найден.")
        if (userFromBalance.amount < amount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недостаточно средств у пользователя для обмена.")
        }

        // Проверяем, достаточно ли средств в терминале для валюты TO
        val terminalToBalance: ExchangerBalance = exchangerBalanceRepository.findByCurrencyCode(toCurrency.uppercase())
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Баланс терминала для валюты $toCurrency не найден.")
        if (terminalToBalance.amount < exchangeAmount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недостаточно средств в терминале для обмена.")
        }

        // Обновляем балансы пользователя:
        // Списываем у пользователя сумму "amount" в валюте FROM
        userFromBalance.amount -= amount
        // За зачисление валюты TO ищем существующий баланс или создаём новый
        var userToBalance = user.balances.find { it.currencyCode == toCurrency.uppercase() }
        if (userToBalance == null) {
            userToBalance = Balance(toCurrency.uppercase(), exchangeAmount, user)
            user.balances.add(userToBalance)
        } else {
            userToBalance.amount += exchangeAmount
        }

        // Обновляем балансы терминала:
        // Уменьшаем баланс терминала валюты TO на exchangeAmount
        terminalToBalance.amount -= exchangeAmount
        // Увеличиваем баланс терминала валюты FROM на сумму "amount".
        var terminalFromBalance = exchangerBalanceRepository.findByCurrencyCode(fromCurrency.uppercase())
        if (terminalFromBalance == null) {
            terminalFromBalance = ExchangerBalance(fromCurrency.uppercase(), amount)
            exchangerBalanceRepository.save(terminalFromBalance)
        } else {
            terminalFromBalance.amount += amount
        }

        // Создаём транзакцию: с отрицательным изменением для списания валюты FROM и положительным для валюты TO.
        val transaction = Transaction(
            user = user,
            currencyPair = "${currencyPair.baseCurrency}/${currencyPair.quoteCurrency}",
            rate = currencyPair.rate,
            fromDelta = -amount,
            toDelta = exchangeAmount
        )
        transactionRepository.save(transaction)

        // Обновляем курсы после обмена
        rateUpdater.updateRates()

        // Сохраняем изменения пользователя
        userRepository.save(user)

        return ResponseEntity.ok(transaction)
    }
}
