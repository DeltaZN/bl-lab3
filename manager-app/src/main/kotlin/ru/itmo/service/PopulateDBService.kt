package ru.itmo.service

import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.itmo.auth.ERole
import ru.itmo.repository.*

@Service
class PopulateDBService(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val roleManager = Role(name = ERole.ROLE_MANAGER)
        val roleAdmin = Role(name = ERole.ROLE_ADMIN)
        roleRepository.save(roleManager)
        roleRepository.save(roleAdmin)
        val admin = EUser(0, "admin", passwordEncoder.encode("666666"), null, setOf(roleAdmin))
        userRepository.save(admin)
    }
}