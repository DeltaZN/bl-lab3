package ru.itmo.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import ru.itmo.auth.ERole
import ru.itmo.auth.JwtUtils
import ru.itmo.auth.UserDetailsImpl
import ru.itmo.messages.BorrowerData
import ru.itmo.messages.ManagerData
import ru.itmo.repository.*
import ru.itmo.service.UserService
import java.util.stream.Collectors
import javax.persistence.EntityNotFoundException

data class RegisterUserRequest(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class MessageIdResponse(
    val message: String,
    val id: Long? = null
)

data class JwtResponse(
    val login: String,
    val firstName: String,
    val lastName: String,
    val roles: Collection<String>,
    val accessToken: String
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api")
@RestController
class AuthController(
    private val managerRepository: ManagerRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val encoder: PasswordEncoder,
    private val userService: UserService
) {

    companion object {
        fun mapBorrowerData(borrower: Borrower): BorrowerData =
            BorrowerData(borrower.id, borrower.firstName, borrower.lastName, borrower.passportData)

        fun mapManagerData(manager: Manager): ManagerData =
            ManagerData(manager.id, manager.firstName, manager.lastName)
    }

    @PostMapping("/signin")
    fun authenticateUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<*>? {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.login, loginRequest.password)
        )
        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtUtils.generateJwtToken(authentication)
        val userDetails = authentication.principal as UserDetailsImpl
        val user = userRepository.findByLogin(userDetails.username)
            .orElseThrow { EntityNotFoundException("User not found") }
        return ResponseEntity.ok(JwtResponse(
            user.login,
            user?.manager?.firstName ?: "",
            user?.manager?.lastName ?: "",
            userDetails.authorities.stream()
                .map { v -> v.authority }
                .collect(Collectors.toList()),
            jwt
        ))
    }


    @PostMapping("/manager/register")
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun registerManager(@RequestBody payload: RegisterUserRequest): MessageIdResponse {
        if (userRepository.findByLogin(payload.login).isPresent)
            throw IllegalStateException("User already registered")
        val manager = Manager(0, payload.firstName, payload.lastName)
        val user = EUser(
            0, payload.login, encoder.encode(payload.password), manager,
            setOf(roleRepository.findRoleByName(ERole.ROLE_MANAGER).get())
        )
        manager.EUser = user
        userRepository.save(user)
        managerRepository.save(manager)
        return MessageIdResponse("Successfully registered manager", manager.id)
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun registerAdmin(@RequestBody payload: RegisterUserRequest): MessageIdResponse {
        if (userRepository.findByLogin(payload.login).isPresent)
            throw IllegalStateException("User already registered")
        var manager = Manager(0, payload.firstName, payload.lastName)
        val user = EUser(
            0, payload.login, encoder.encode(payload.password), manager,
            setOf(roleRepository.findRoleByName(ERole.ROLE_ADMIN).get())
        )
        manager.EUser = user
        userRepository.save(user)
        manager = managerRepository.save(manager)
        return MessageIdResponse("Successfully registered admin", manager.id)
    }

    @GetMapping("/manager/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getManagerData(@PathVariable id: Long): ManagerData {
        userService.checkManagerAuthority(id)
        val manager = managerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Manager with id $id not found!")
        }
        return mapManagerData(manager)
    }

}