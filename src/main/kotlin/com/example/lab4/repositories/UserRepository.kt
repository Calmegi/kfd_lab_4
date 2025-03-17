package com.example.lab4.repositories

import com.example.lab4.db.ExchangerUser
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<ExchangerUser, Long> {
    fun findByUsername(username: String): ExchangerUser?
}
