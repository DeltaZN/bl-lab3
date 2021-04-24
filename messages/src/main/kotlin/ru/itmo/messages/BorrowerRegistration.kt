package ru.itmo.messages

const val KAFKA_BORROWER_TOPIC = "server.borrower_data"
// client-app -> manager-app
data class BorrowerData(
    val id: Long,
    var firstName: String,
    var lastName: String,
    var passportSeriesAndNumber: String,
)