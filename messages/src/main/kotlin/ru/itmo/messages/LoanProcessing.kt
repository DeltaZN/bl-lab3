package ru.itmo.messages

import java.time.LocalDateTime

enum class LoanRequestStatus {
    NEW,
    APPROVED,
    REJECTED,
}

enum class LoanStatus {
    NORMAL,
    EXPIRED,
    CLOSED,
}

// client-app -> manager-app
data class LoanRequest (
    val id: Long,
    val sum: Double,
    val percent: Double,
    val loanDays: Int,
    val borrowerId: Long,
)

// manager-app -> client-app
data class LoanProcessed (
    val loanId: Long,
    val loanStatus: LoanRequestStatus,
    val newLoan: Loan?,
)

// auxiliary class
data class Loan(
    val id: Long,
    val sum: Double,
    val percent: Double,
    val startDate: LocalDateTime,
    val finishDate: LocalDateTime,
    val loanStatus: LoanStatus,
    val borrowerId: Long,
)