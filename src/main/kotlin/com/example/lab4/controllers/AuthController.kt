package com.example.lab4.controllers

import com.example.lab4.db.ExchangerUser
//import com.example.lab4.db.repositories.UserRepository
import com.example.lab4.repositories.UserRepository
import com.example.lab4.security.CustomUserDetailsService
import com.example.lab4.security.JwtUtil
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

/*
Этот контроллер предоставляет REST‑эндпоинты по адресу /api/auth.
Он отвечает за регистрацию новых пользователей (signup) и вход (signin).
*/

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userService: CustomUserDetailsService,
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    // Регистрация нового пользователя
    @PostMapping("/signup")
    fun registerUser(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<Any> {
        // Если такой пользователь уже есть – возвращаем ошибку
        val existingUser = userRepository.findByUsername(registerRequest.username)
        if (existingUser != null) {
            return ResponseEntity.badRequest().body("Пользователь с таким именем уже существует.")
        }
        // По умолчанию назначаем роль ROLE_USER.
        val authorities: MutableList<String> = mutableListOf("ROLE_USER")
        // Если аутентифицированный пользователь имеет роль ADMIN и переданы дополнительные роли – добавляем их.
        if (SecurityContextHolder.getContext().authentication?.authorities?.any { it.authority == "ROLE_ADMIN" } == true &&
            registerRequest.authorities != null
        ) {
            authorities.addAll(registerRequest.authorities)
        }
        // Создаём нового пользователя, шифруя пароль.
        val newUser = ExchangerUser(
            username = registerRequest.username,
            password = encoder.encode(registerRequest.password),
            authorities = authorities
        )
        // Инициализация баланса пользователя: стартовый баланс RUB = 1000000 * 100 (с учётом копеек)
        newUser.balances.add(com.example.lab4.db.Balance("RUB", 1000000 * 100, newUser))
        val savedUser = userRepository.save(newUser)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser)
    }

    // Вход пользователя – аутентификация и выдача JWT.
    @PostMapping("/signin")
    fun loginUser(@Valid @RequestBody request: AuthenticationRequest): ResponseEntity<String> {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
        } catch (ex: BadCredentialsException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверное имя пользователя или пароль.")
        }
        // Генерация JWT после успешной аутентификации.
        val userDetails = userService.loadUserByUsername(request.username)
        val token = jwtUtil.generateToken(userDetails.username)
        return ResponseEntity.ok(token)
    }
}

// Модель запроса для аутентификации.
data class AuthenticationRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val password: String
)

// Модель запроса для регистрации.
data class RegisterRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val password: String,
    val authorities: List<String>? = null
)
