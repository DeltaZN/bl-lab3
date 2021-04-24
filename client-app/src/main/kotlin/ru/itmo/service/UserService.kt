package ru.itmo.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.itmo.auth.ERole
import ru.itmo.auth.UserDetailsImpl
import ru.itmo.repository.EUser
import ru.itmo.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    private fun getCurrentUserId(): Long = (SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl).id

    fun getUserFromAuth(): EUser = userRepository.findById(getCurrentUserId())
        .orElseThrow { UsernameNotFoundException("User not found - ${getCurrentUserId()}") }

    fun checkBorrowerAuthority(ownerId: Long) {
        val accessor = getUserFromAuth()
        val borrower = accessor.borrower
        if (ownerId != borrower?.id)
            throw IllegalAccessException("Access denied")
    }
}