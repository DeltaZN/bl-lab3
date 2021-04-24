package ru.itmo.messages

// client-app -> manager-app
data class BorrowerData(
    val id: Long,
    var firstName: String,
    var lastName: String,
    var passportSeriesAndNumber: String,
)