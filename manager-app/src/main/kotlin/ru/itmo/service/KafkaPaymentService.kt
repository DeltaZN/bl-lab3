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
class KafkaPaymentService(
    private val kafkaPaymentResultTemplate: KafkaTemplate<Long, PaymentResultDto>,
    private val paymentRepository: PaymentRepository,
    private val loanRepository: LoanRepository,
    private val moneyService: MoneyService
) {
    private val log: Logger = LoggerFactory.getLogger(KafkaPaymentService::class.java)

    fun sendPaymentResult(payment: Payment, loan: Loan) {
        kafkaPaymentResultTemplate.send(
            KAFKA_PAYMENT_RESULT_TOPIC, PaymentResultDto(
                payment.id,
                loan.id,
                payment.status
            )
        )
    }

    @KafkaListener(id = "Payment", topics = [KAFKA_PAYMENT_TOPIC], containerFactory = "singleFactory")
    fun consumeProcessedPayment(dto: PaymentDto) {
        log.info("Kafka payment $dto")
        val loan = loanRepository.findById(dto.loanId).orElseThrow {
            EntityNotFoundException("Loan not found")
        }

        val payment = Payment(dto.paymentId,dto.sum,PaymentStatus.PROCESSING,loan.borrower,loan,dto.paymentDate)


        if (!moneyService.checkMoneyTransaction(loan.borrower))
            payment.status = PaymentStatus.REFUSED
            else payment.status = PaymentStatus.ACCEPTED

        paymentRepository.save(payment)

        if (payment.status == PaymentStatus.ACCEPTED) {
            loan.sum -= payment.sum
            if (loan.sum <= 0.0) {
                loan.loanStatus = LoanStatus.CLOSED
            }
            loanRepository.save(loan)
        }
        sendPaymentResult(payment, loan)

    }
}