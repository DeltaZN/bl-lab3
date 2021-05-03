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
    private val kafkaLoanRequestDtoTemplate: KafkaTemplate<Long, LoanRequestDto>,
    private val loanRepository: LoanRepository,
    private val loanRequestRepository: LoanRequestRepository,
    private val borrowerRepository: BorrowerRepository,
) {
    private val log: Logger = LoggerFactory.getLogger(KafkaLoanService::class.java)

    fun sendLoanRequest(loan: LoanRequest, borrowerId: Long) {
        kafkaLoanRequestDtoTemplate.send(
            KAFKA_LOAN_REQUEST_TOPIC, LoanRequestDto(
                loan.id,
                loan.sum,
                loan.percent,
                loan.loanDays,
                borrowerId,
            )
        )
    }

    @KafkaListener(id = "LoanRequestResult", topics = [KAFKA_LOAN_REQUEST_ANSWER_TOPIC], containerFactory = "singleFactory")
    fun consumeLoanRequestResult(dto: LoanRequestAnswerDto) {
        log.info("Kafka loan $dto")

        val loanRequest = loanRequestRepository.findById(dto.loanRequestId).orElseThrow {
            EntityNotFoundException("Loan request not found")
        }
        loanRequest.requestStatus = dto.loanRequestStatus
        loanRequestRepository.save(loanRequest)

        if (dto.loanRequestStatus == LoanRequestStatus.APPROVED && dto.newLoanDto != null) {
            val loanDto = dto.newLoanDto!!
            val loan = Loan(
                loanDto.id,
                loanDto.sum,
                loanDto.percent,
                loanDto.startDate,
                loanDto.finishDate,
                loanDto.loanStatus,
                borrowerRepository.findById(loanDto.borrowerId).orElseThrow {
                    EntityNotFoundException("Borrower not found")
                }
            )
            loanRepository.save(loan)
        }
    }

    @KafkaListener(id = "Loan", topics = [KAFKA_LOAN_TOPIC], containerFactory = "singleFactory")
    fun consumeLoan(dto: LoanDto) {
        log.info("Kafka loan $dto")

        val loan = loanRepository.findById(dto.id).orElseThrow {
            EntityNotFoundException("Loan not found")
        }

        loan.loanStatus = dto.loanStatus
        loan.sum = dto.sum
        loan.percent = dto.percent

        loanRepository.save(loan)
    }
}