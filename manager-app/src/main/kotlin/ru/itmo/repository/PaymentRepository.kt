package ru.itmo.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.itmo.messages.PaymentStatus
import java.time.LocalDateTime
import javax.persistence.*

interface PaymentRepository : JpaRepository<Payment, Long>

@Entity
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var sum: Double = 0.0,
    var status: PaymentStatus = PaymentStatus.PROCESSING,
    @ManyToOne
    var borrower: Borrower = Borrower(),
    @ManyToOne
    var loan: Loan = Loan(),
    var paymentDate: LocalDateTime = LocalDateTime.now()
)