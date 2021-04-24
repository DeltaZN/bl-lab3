package ru.itmo.messages

import java.time.LocalDateTime

enum class PaymentStatus {
    PROCESSING,
    ACCEPTED,
    REFUSED,
}

// client-app -> manager-app
data class Payment(
    val paymentId: Long,
    val loanId: Long,
    val sum: Double,
    val paymentDate: LocalDateTime,
)

// manager-app -> client-app
data class PaymentProcessingResult(
    val paymentId: Long,
    val loanId: Long,
    val status: PaymentStatus,
)