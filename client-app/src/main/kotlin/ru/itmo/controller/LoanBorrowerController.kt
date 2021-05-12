package ru.itmo.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.itmo.messages.PaymentStatus
import ru.itmo.repository.LoanRepository
import ru.itmo.repository.Payment
import ru.itmo.repository.PaymentRepository
import ru.itmo.service.KafkaPaymentService
import ru.itmo.service.UserService
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

data class ProcessPaymentRequest(
    val sum: Double,
    val loanId: Long
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/borrower/loan/")
@RestController
class LoanBorrowerController(
    private val loanRepository: LoanRepository,
    private val userService: UserService,
    private val paymentRepository: PaymentRepository,
    private val kafkaPaymentService: KafkaPaymentService,
) {
    @PostMapping("pay")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun processPayment(@RequestBody payload: ProcessPaymentRequest): MessageIdResponse {
        val loan = loanRepository.findById(payload.loanId).orElseThrow {
            EntityNotFoundException("Loan with id ${payload.loanId} not found!")
        }

        userService.checkBorrowerAuthority(loan.borrower.id)
        val payment = Payment(0, payload.sum, PaymentStatus.PROCESSING, userService.getUserFromAuth().borrower!!, loan)
        paymentRepository.save(payment)

        kafkaPaymentService.sendPayment(payment, loan.id)

        return MessageIdResponse("Payment accepted, wait for processing...")
    }

    data class PaymentDto(
        var id: Long = 0,
        var sum: Double = 0.0,
        var status: PaymentStatus = PaymentStatus.PROCESSING,
        var loanId: Long = 0,
        var paymentDate: LocalDateTime = LocalDateTime.now(),
    )

    @GetMapping("payments")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun getPayments(): List<PaymentDto> = paymentRepository
        .findPaymentsByBorrower(userService.getUserFromAuth().borrower!!)
        .map { p -> PaymentDto(p.id, p.sum, p.status, p.loan.id, p.paymentDate) }
}