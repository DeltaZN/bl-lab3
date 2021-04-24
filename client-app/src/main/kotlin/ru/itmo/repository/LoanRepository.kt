package ru.itmo.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.itmo.messages.LoanStatus
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Loan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var sum: Double = 0.0,
    var percent: Double = 0.0,
    var startDate: LocalDateTime = LocalDateTime.now(),
    var finishDate: LocalDateTime = LocalDateTime.now(),
    var loanStatus: LoanStatus = LoanStatus.NORMAL,
    @ManyToOne
    var borrower: Borrower = Borrower(),
    var loanReqId: Long = 0
)

interface LoanRepository : JpaRepository<Loan, Long> {
    fun findLoansByLoanStatus(status: LoanStatus): List<Loan>
    fun findLoansByBorrower(borrower: Borrower): List<Loan>
}