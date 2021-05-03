package ru.itmo.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import ru.itmo.auth.ERole
import ru.itmo.repository.Role
import ru.itmo.repository.RoleRepository

@Service
class PopulateDBService(
    private val roleRepository: RoleRepository,
) : CommandLineRunner {

    @Value("\${spring.jpa.hibernate.ddl-auto}")
    private val ddlAuto: String = "";

    override fun run(vararg args: String?) {
        if (ddlAuto == "create") {
            val roleBorrower = Role(name = ERole.ROLE_BORROWER)
            val roleBorrowerConfirmed = Role(name = ERole.ROLE_BORROWER_CONFIRMED)
            roleRepository.save(roleBorrower)
            roleRepository.save(roleBorrowerConfirmed)
        }
    }
}