package com.example.lab4.controllers

import com.example.lab4.db.ExchangerUser
import com.example.lab4.repositories.UserRepository
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.parameters.P
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/users")
class UsersController(
    private val userRepository: UserRepository
) {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getUsers(): ResponseEntity<List<ExchangerUser>> {
        return ResponseEntity.ok(userRepository.findAll())
    }

    @PreAuthorize("authentication.name == #username || hasRole('ADMIN')")
    @GetMapping("/{username}")
    fun getUser(@PathVariable @P("username") username: String): ResponseEntity<ExchangerUser> {
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user)
    }

    @PreAuthorize("authentication.name == #username || hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    fun deleteUser(@PathVariable @P("username") username: String): ResponseEntity<Void> {
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.notFound().build()
        userRepository.delete(user)
        return ResponseEntity.ok().build()
    }
}
