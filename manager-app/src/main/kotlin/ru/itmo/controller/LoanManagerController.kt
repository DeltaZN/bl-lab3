package ru.itmo.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.*
import ru.itmo.messages.BorrowerData
import ru.itmo.messages.LoanRequestStatus
import ru.itmo.messages.LoanStatus
import ru.itmo.messages.ManagerData
import ru.itmo.repository.BorrowerRepository
import ru.itmo.repository.Loan
import ru.itmo.repository.LoanRepository
import ru.itmo.repository.LoanRequestRepository
import ru.itmo.service.*
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

data class ManageLoanRequest(
    val loanReqId: Long
)

class ProcessPaymentException(msg: String) : RuntimeException(msg)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/loan/")
@RestController
class LoanManagerController(
    private val loanRequestRepository: LoanRequestRepository,
    private val loanRepository: LoanRepository,
    private val moneyService: MoneyService,
    private val borrowerRepository: BorrowerRepository,
    private val comms: CommunicationService,
    private val template: TransactionTemplate,
    private val userService: UserService,
    private val kafkaLoanService: KafkaLoanService
) {

    data class LoanData(
        val id: Long,
        val sum: Double,
        val percent: Double,
        val startDate: LocalDateTime,
        val finishDate: LocalDateTime,
        val loanStatus: LoanStatus,
        val borrower: BorrowerData,
        val approver: ManagerData,
        val loanReqId: Long
    )

    companion object {
        fun mapLoanData(loan: Loan): LoanData =
            LoanData(
                loan.id, loan.sum, loan.percent, loan.startDate, loan.finishDate,
                loan.loanStatus, AuthController.mapBorrowerData(loan.borrower),
                AuthController.mapManagerData(loan.approver), loan.loanReqId
            )
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun approveLoan(@RequestBody payload: ManageLoanRequest): MessageIdResponse {
        val manager = userService.getUserFromAuth().manager!!
        val loanRequest = loanRequestRepository.findById(payload.loanReqId).orElseThrow {
            EntityNotFoundException("Loan request with id ${payload.loanReqId} not found!")
        }
        if (loanRequest.requestStatus != LoanRequestStatus.NEW)
            throw EntityNotFoundException("Loan request with id ${payload.loanReqId} already ${loanRequest.requestStatus}")


        return template.execute {
            val loan = Loan(
                0,
                loanRequest.sum,
                loanRequest.percent,
                finishDate = LocalDateTime.now().plusDays(loanRequest.loanDays.toLong()),
                borrower = loanRequest.borrower,
                approver = manager,
                loanReqId = loanRequest.id
            )
            loanRequest.requestStatus = LoanRequestStatus.APPROVED
            loanRequestRepository.save(loanRequest)
            loanRepository.save(loan)
            moneyService.sendMoney(loanRequest.borrower, loanRequest.sum)
            comms.sendNotificationToBorrower(Notification(loan.id, "Your loan has been approved"), loanRequest.borrower)
            kafkaLoanService.sendLoanRequestResult(loanRequest, loan)
            //todo распределенная транзакция?
            MessageIdResponse("Loan request approved", loan.id)

        } ?: throw ProcessPaymentException("oops")
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun rejectLoan(@RequestBody payload: ManageLoanRequest): MessageIdResponse {
        val loanRequest = loanRequestRepository.findById(payload.loanReqId).orElseThrow {
            EntityNotFoundException("Loan request with id ${payload.loanReqId} not found!")
        }
        if (loanRequest.requestStatus != LoanRequestStatus.NEW)
            throw EntityNotFoundException("Loan request with id ${payload.loanReqId} already ${loanRequest.requestStatus}")

        loanRequest.requestStatus = LoanRequestStatus.REJECTED
        loanRequestRepository.save(loanRequest)

        comms.sendNotificationToBorrower(
            Notification(loanRequest.id, "Your loan has been rejected"),
            loanRequest.borrower
        )
        kafkaLoanService.sendLoanRequestResult(loanRequest, null)
        return MessageIdResponse("Loan request rejected", loanRequest.id)
    }

    @GetMapping("/borrower/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getBorrowerLoans(@PathVariable id: Long): List<LoanData> {
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return loanRepository.findLoansByBorrower(borrower)
            .map { l -> mapLoanData(l) }
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getLoan(@PathVariable id: Long): LoanData {
        val loan = loanRepository.findById(id).orElseThrow {
            EntityNotFoundException("Loan with id $id not found!")
        }
        return mapLoanData(loan)
    }

}