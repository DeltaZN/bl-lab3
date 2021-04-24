package ru.itmo.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.itmo.auth.AuthEntryPointJwt
import ru.itmo.messages.*
import ru.itmo.repository.LoanRepository
import ru.itmo.repository.LoanRequestRepository
import ru.itmo.repository.Payment
import ru.itmo.repository.PaymentRepository
import javax.persistence.EntityNotFoundException

@Service
class KafkaPaymentService(
    private val kafkaPaymentTemplate: KafkaTemplate<Long, PaymentDto>,
    private val paymentRepository: PaymentRepository,
    private val loanRepository: LoanRepository,
) {
    private val log: Logger = LoggerFactory.getLogger(KafkaPaymentService::class.java)

    fun sendPayment(payment: Payment, loanId: Long) {
        kafkaPaymentTemplate.send(
            KAFKA_PAYMENT_TOPIC, PaymentDto(
                payment.id,
                loanId,
                payment.sum,
                payment.paymentDate,
            )
        )
    }

    @KafkaListener(id = "PaymentProcessingResult", topics = [KAFKA_PAYMENT_RESULT_TOPIC], containerFactory = "singleFactory")
    fun consumeProcessedPayment(dto: PaymentResultDto) {
        log.info("Kafka payment $dto")

        val payment = paymentRepository.findById(dto.paymentId).orElseThrow {
            EntityNotFoundException("Payment not found")
        }
        val loan = loanRepository.findById(dto.loanId).orElseThrow {
            EntityNotFoundException("Loan not found")
        }
        payment.status = dto.status
        paymentRepository.save(payment)

        if (dto.status == PaymentStatus.ACCEPTED) {
            loan.sum -= payment.sum
            if (loan.sum <= 0.0) {
                loan.loanStatus = LoanStatus.CLOSED
            }
            loanRepository.save(loan)
        }
    }
}