package com.example.lab4.controllers

import com.example.lab4.db.ExchangerBalance
import com.example.lab4.repositories.ExchangerBalanceRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/*
  Контроллер ExchangerBalanceController обеспечивает два эндпоинта:
  • GET /api/exchangerbalance – получение баланса терминала (доступно любому аутентифицированному пользователю);
  • POST /api/exchangerbalance – заполнение (обновление/добавление) балансов терминала (только для администратора).
*/

@RestController
@RequestMapping("/api/exchangerbalance")
class ExchangerBalanceController(
    private val exchangerBalanceRepository: ExchangerBalanceRepository
) {

    // Эндпоинт, позволяющий любому аутентифицированному пользователю (например, bob)
    // получить балансы терминала
    @GetMapping
    fun getExchangerBalances(): ResponseEntity<List<ExchangerBalance>> {
        val balances = exchangerBalanceRepository.findAll()
        return ResponseEntity.ok(balances)
    }

    // Эндпоинт для админа для обновления (пополнения) баланса терминала.
    // Используем POST-запрос с заголовком Content-Type: application/json.
    @PostMapping(consumes = ["application/json"])
    @PreAuthorize("hasRole('ADMIN')")
    fun updateExchangerBalances(
        @RequestBody @Valid requests: List<ExchangerBalanceRequest>
    ): ResponseEntity<List<ExchangerBalance>> {
        // Для каждого запроса обновляем или создаём баланс для соответствующей валюты
        requests.forEach { req ->
            // Приводим код валюты к верхнему регистру
            val currency = req.currencyCode.uppercase()
            var terminalBalance = exchangerBalanceRepository.findByCurrencyCode(currency)
            if (terminalBalance == null) {
                // Если баланс для валюты отсутствует – создаём новый
                terminalBalance = ExchangerBalance(currency, req.amount)
                exchangerBalanceRepository.save(terminalBalance)
            } else {
                // Иначе, увеличиваем текущий баланс на запрошенную сумму
                terminalBalance.amount += req.amount
                exchangerBalanceRepository.save(terminalBalance)
            }
        }
        // Возвращаем обновлённый список балансов терминала
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
