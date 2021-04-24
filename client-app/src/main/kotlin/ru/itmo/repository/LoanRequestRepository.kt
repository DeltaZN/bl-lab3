package ru.itmo.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.itmo.messages.LoanRequestStatus
import javax.persistence.*

@Entity
class LoanRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var sum: Double = 0.0,
    var requestStatus: LoanRequestStatus = LoanRequestStatus.NEW,
    var percent: Double = 0.0,
    var loanDays: Int = 0,
    @ManyToOne
    var borrower: Borrower = Borrower()
)

interface LoanRequestRepository : JpaRepository<LoanRequest, Long> {
    fun findLoansByBorrower(borrower: Borrower): List<LoanRequest>
}