package ru.itmo.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.*
import ru.itmo.messages.PaymentStatus
import ru.itmo.repository.*
import ru.itmo.service.UserService
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
        // TODO send to manager-app
        return MessageIdResponse("Payment accepted, wait for processing...")
    }
}