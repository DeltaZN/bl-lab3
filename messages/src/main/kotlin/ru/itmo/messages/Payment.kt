package ru.itmo.messages

import java.time.LocalDateTime

enum class PaymentStatus {
    PROCESSING,
    ACCEPTED,
    REFUSED,
}

const val KAFKA_PAYMENT_TOPIC = "server.payment"
// client-app -> manager-app
data class Payment(
    val paymentId: Long,
    val loanId: Long,
    val sum: Double,
    val paymentDate: LocalDateTime,
)

const val KAFKA_PAYMENT_RESULT_TOPIC = "server.payment_result"
// manager-app -> client-app
data class PaymentProcessingResult(
    val paymentId: Long,
    val loanId: Long,
    val status: PaymentStatus,
)