package ru.itmo.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.itmo.messages.LoanStatus
import ru.itmo.messages.PaymentStatus
import ru.itmo.repository.*
import ru.itmo.service.KafkaPaymentService
import ru.itmo.service.UserService
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

data class ProcessPaymentRequest(
    val sum: Double,
    val loanId: Long
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/loan/")
@RestController
class LoanBorrowerController(
    private val loanRepository: LoanRepository,
    private val userService: UserService,
    private val paymentRepository: PaymentRepository,
    private val kafkaPaymentService: KafkaPaymentService,
    private val borrowerRepository: BorrowerRepository
) {
    companion object {
        fun mapLoanData(loan: Loan): LoanData =
                LoanData(loan.id, loan.sum, loan.percent, loan.startDate,
                        loan.finishDate, loan.loanStatus, AuthController.mapBorrowerData(loan.borrower))
    }

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
    fun getPayments(): List<PaymentDto> {
      return  paymentRepository
                .findPaymentsByBorrower(userService.getUserFromAuth().borrower!!)
                .map { p -> PaymentDto(p.id, p.sum, p.status, p.loan.id, p.paymentDate) }
    }

    data class LoanData(
            val id: Long,
            val sum: Double,
            val percent: Double,
            val startDate: LocalDateTime,
            val finishDate: LocalDateTime,
            val loanStatus: LoanStatus,
            val borrower: AuthController.BorrowerData
            )

    @GetMapping("/borrower/{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun getBorrowerLoans(@PathVariable id: Long): List<LoanData> {
        userService.checkBorrowerAuthority(id)
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return loanRepository.findLoansByBorrower(borrower)
                .map { l -> mapLoanData(l) }
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun getLoan(@PathVariable id: Long): LoanData {
        val request = loanRepository.findById(id).orElseThrow {
            EntityNotFoundException("Loan with id $id not found!")
        }
        userService.checkBorrowerAuthority(request.borrower.id)
        return mapLoanData(request)
    }
}