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

const val KAFKA_LOAN_REQUEST_TOPIC = "server.loan_request"
// client-app -> manager-app
data class LoanRequestDto (
    val id: Long,
    val sum: Double,
    val percent: Double,
    val loanDays: Int,
    val borrowerId: Long,
)

const val KAFKA_LOAN_REQUEST_ANSWER_TOPIC = "server.loan_request_answer"
// manager-app -> client-app
data class LoanRequestAnswerDto (
    val loanRequestId: Long,
    val loanRequestStatus: LoanRequestStatus,
    val newLoanDto: LoanDto?,
)

const val KAFKA_LOAN_TOPIC = "server.loan"
// manager-app -> client-app
data class LoanDto(
    val id: Long,
    val sum: Double,
    val percent: Double,
    val startDate: LocalDateTime,
    val finishDate: LocalDateTime,
    val loanStatus: LoanStatus,
    val borrowerId: Long,
)