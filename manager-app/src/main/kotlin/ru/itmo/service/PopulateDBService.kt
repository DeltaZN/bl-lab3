package ru.itmo.service

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import ru.itmo.auth.ERole
import ru.itmo.repository.Role
import ru.itmo.repository.RoleRepository

@Service
class PopulateDBService(
    private val roleRepository: RoleRepository
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val roleManager = Role(name = ERole.ROLE_MANAGER)
        val roleAdmin = Role(name = ERole.ROLE_ADMIN)
        roleRepository.save(roleManager)
        roleRepository.save(roleAdmin)
    }
}