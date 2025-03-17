package com.example.lab4.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val userDetailsService: CustomUserDetailsService,
    private val jwtUtil: JwtUtil,
    private val authenticationConfiguration: AuthenticationConfiguration
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // Отключаем CSRF (для упрощения тестирования; для production‑решений CSRF следует настраивать отдельно)
        http.csrf { it.disable() }

        http.authorizeHttpRequests { auth ->
            // Доступ к эндпоинтам аутентификации без токена
            auth.requestMatchers("/api/auth/**").permitAll()

            // Административные операции только через POST:
            // – Обновление балансов (пополнение баланса пользователя или терминала)
            // – Добавление валютных пар и обновление курсов
            auth.requestMatchers(
                HttpMethod.POST,
                "/api/balance/**",
                "/api/exchangerbalance/**",
                "/api/rates/**"
            ).hasRole("ADMIN")

            // Остальные запросы доступны любым аутентифицированным пользователям.
            auth.anyRequest().authenticated()
        }

        // Добавляем фильтр для обработки JWT перед стандартным фильтром аутентификации
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(userDetailsService, jwtUtil)
    }

    @Bean
    fun daoAuthenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun authenticationManager(): AuthenticationManager =
        authenticationConfiguration.authenticationManager
}
