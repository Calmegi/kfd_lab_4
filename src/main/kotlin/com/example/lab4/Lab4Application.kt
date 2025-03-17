package com.example.lab4

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// Главная аннотация для Spring Boot
@SpringBootApplication
class Lab4Application

// Функция main – точка входа в приложение.
fun main(args: Array<String>) {
    runApplication<Lab4Application>(*args)
}
