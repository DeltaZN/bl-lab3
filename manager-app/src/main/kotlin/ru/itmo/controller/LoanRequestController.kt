package ru.itmo.controller

import org.springframework.web.bind.annotation.*
import ru.itmo.messages.BorrowerData
import ru.itmo.messages.LoanRequestStatus
import ru.itmo.repository.BorrowerRepository
import ru.itmo.repository.LoanRequest
import ru.itmo.repository.LoanRequestRepository
import javax.persistence.EntityNotFoundException



@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/loan")
@RestController
class LoanRequestController(
    private val loanRequestRepository: LoanRequestRepository,
    private val borrowerRepository: BorrowerRepository
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
        val borrower: BorrowerData
    )

    @GetMapping("/borrower/{id}")
    fun getBorrowerLoans(@PathVariable id: Long): List<LoanRequestData> {
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return loanRequestRepository.findLoansByBorrower(borrower)
            .map { l -> mapLoanRequestData(l) }
    }

    @GetMapping("{id}")
    fun getLoan(@PathVariable id: Long): LoanRequestData {
        val request = loanRequestRepository.findById(id).orElseThrow {
            EntityNotFoundException("Loan req with id $id not found!")
        }
        return mapLoanRequestData(request)
    }
}