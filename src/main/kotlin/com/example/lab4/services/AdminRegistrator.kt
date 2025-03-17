package com.example.lab4.services

import com.example.lab4.db.ExchangerUser
//import com.example.lab4.db.repositories.UserRepository
import com.example.lab4.repositories.UserRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/*
Сервис регистрирует администратора при запуске приложения.
Если пользователь с username "admin" отсутствует, он создаётся с логином и паролем "admin".
Также инициализируется стартовый баланс.
*/

@Service
class AdminRegistrator(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @EventListener(ApplicationReadyEvent::class)
    fun registerAdmin() {
        if (userRepository.findByUsername("admin") != null) {
            return
        }
        val adminUser = ExchangerUser(
            username = "admin",
            password = passwordEncoder.encode("admin"),
            authorities = listOf("ROLE_USER", "ROLE_ADMIN")
        )
        // Инициализация баланса для администратора.
        adminUser.balances.add(com.example.lab4.db.Balance("RUB", 1000000 * 100, adminUser))
        userRepository.save(adminUser)
    }
}
