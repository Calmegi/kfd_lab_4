package com.example.lab4.security

import com.example.lab4.repositories.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/*
Сервис для загрузки деталей пользователя, используемый Spring Security.
Он ищет пользователя по имени и возвращает объект UserDetails.
*/

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        val user = userRepository.findByUsername(username ?: "")
            ?: throw UsernameNotFoundException("Пользователь $username не найден")
        val authorities: List<GrantedAuthority> = user.authorities.map { SimpleGrantedAuthority(it) }
        return User(user.username, user.password, authorities)
    }
}
