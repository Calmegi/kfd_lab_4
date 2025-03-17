package com.example.lab4.repositories

import com.example.lab4.db.Transaction
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionRepository : JpaRepository<Transaction, Long> {
    fun findByUserUsername(username: String): List<Transaction>
}
