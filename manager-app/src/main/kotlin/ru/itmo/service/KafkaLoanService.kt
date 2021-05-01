package ru.itmo.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.itmo.messages.*
import ru.itmo.repository.*
import javax.persistence.EntityNotFoundException

@Service
class KafkaLoanService(
    private val kafkaLoanRequestAnswerDtoTemplate: KafkaTemplate<Long, LoanRequestAnswerDto>,
    private val kafkaLoanDtoTemplate: KafkaTemplate<Long, LoanDto>,
    private val loanRequestRepository: LoanRequestRepository,
    private val borrowerRepository: BorrowerRepository,
    private val comms: CommunicationService
) {
    private val log: Logger = LoggerFactory.getLogger(KafkaLoanService::class.java)

    fun sendLoanRequestResult(loanReq: LoanRequest, loan: Loan?) {
        var loanDto: LoanDto? = null
        if (loan != null)
            loanDto = LoanDto(
                loan.id,
                loan.sum,
                loan.percent,
                loan.startDate,
                loan.finishDate,
                loan.loanStatus,
                loan.borrower.id
            )
        kafkaLoanRequestAnswerDtoTemplate.send(
            KAFKA_LOAN_REQUEST_ANSWER_TOPIC, LoanRequestAnswerDto(
                loanReq.id,
                loanReq.requestStatus,
                loanDto
            )
        )
    }

    fun sendLoan(loan: Loan) {
        kafkaLoanDtoTemplate.send(
            KAFKA_LOAN_TOPIC, LoanDto(
                loan.id,
                loan.sum,
                loan.percent,
                loan.startDate,
                loan.finishDate,
                loan.loanStatus,
                loan.borrower.id
            )
        )
    }


    @KafkaListener(id = "Loan", topics = [KAFKA_LOAN_REQUEST_TOPIC], containerFactory = "singleFactory")
    fun consumeLoanRequest(dto: LoanRequestDto) {
        log.info("kafka loanRequest consumer $dto")
        val borrower = borrowerRepository.findById(dto.borrowerId).orElseThrow {
            EntityNotFoundException("Borrower not found")
        }
        val loanRequest = LoanRequest(dto.id, dto.sum, LoanRequestStatus.NEW, dto.percent, dto.loanDays, borrower)
        loanRequestRepository.save(loanRequest)
        comms.broadcastNotificationToManagers(Notification(dto.id, "A loan awaits for approval"))
    }
}