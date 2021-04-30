package ru.itmo.repository

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
class Manager(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "first_name")
    var firstName: String = "",
    @Column(name = "last_name")
    var lastName: String = "",
    @OneToOne(mappedBy = "manager")
    var EUser: EUser = EUser()
)

interface ManagerRepository : JpaRepository<Manager, Long>