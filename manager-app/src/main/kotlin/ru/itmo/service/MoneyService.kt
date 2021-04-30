package ru.itmo.service

import org.springframework.stereotype.Service
import ru.itmo.repository.Borrower

interface MoneyService {
    fun sendMoney(borrower: Borrower, money: Double)
    fun checkMoneyTransaction(borrower: Borrower): Boolean
}

@Service
class MoneyServiceStub : MoneyService {
    override fun sendMoney(borrower: Borrower, money: Double) {}
    override fun checkMoneyTransaction(borrower: Borrower) = true
}