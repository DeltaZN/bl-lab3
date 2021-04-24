package ru.itmo.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.itmo.messages.LoanRequestStatus
import ru.itmo.repository.BorrowerRepository
import ru.itmo.repository.LoanRequest
import ru.itmo.repository.LoanRequestRepository
import ru.itmo.service.KafkaLoanService
import ru.itmo.service.UserService
import javax.persistence.EntityNotFoundException

data class LoanRequestPayload(
    val sum: Double,
    val percent: Double,
    val loanDays: Int
)

data class LoanResponse(
    val message: String,
    val id: Long? = null
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/loan")
@RestController
class LoanRequestController(
    private val loanRequestRepository: LoanRequestRepository,
    private val borrowerRepository: BorrowerRepository,
    private val userService: UserService,
    private val kafkaLoanService: KafkaLoanService,
) {

    companion object {
        fun mapLoanRequestData(loan: LoanRequest): LoanRequestData =
            LoanRequestData(loan.id, loan.sum, loan.requestStatus, loan.percent, loan.loanDays,
                AuthController.mapBorrowerData(loan.borrower))
    }

    data class LoanRequestData(
        val id: Long,
        val sum: Double,
        val requestStatus: LoanRequestStatus,
        val percent: Double,
        val loanDays: Int,
        val borrower: AuthController.BorrowerData,
    )

    @GetMapping("/borrower/{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun getBorrowerLoans(@PathVariable id: Long): List<LoanRequestData> {
        userService.checkBorrowerAuthority(id)
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return loanRequestRepository.findLoansByBorrower(borrower)
            .map { l -> mapLoanRequestData(l) }
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun getLoan(@PathVariable id: Long): LoanRequestData {
        val request = loanRequestRepository.findById(id).orElseThrow {
            EntityNotFoundException("Loan with id $id not found!")
        }
        userService.checkBorrowerAuthority(request.borrower.id)
        return mapLoanRequestData(request)
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun makeLoan(@RequestBody payload: LoanRequestPayload): LoanResponse {
        val borrower = userService.getUserFromAuth().borrower!!
        val loan = LoanRequest(0, payload.sum, LoanRequestStatus.NEW, payload.percent, payload.loanDays, borrower)
        loanRequestRepository.save(loan)

        kafkaLoanService.sendLoanRequest(loan, borrower.id)

        return LoanResponse("Wait for loan approval", loan.id)
    }
}