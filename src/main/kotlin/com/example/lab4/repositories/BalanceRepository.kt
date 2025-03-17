package com.example.lab4.repositories

import com.example.lab4.db.Balance
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceRepository : JpaRepository<Balance, Long>
