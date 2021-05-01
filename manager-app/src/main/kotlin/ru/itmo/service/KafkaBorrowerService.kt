package ru.itmo.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.itmo.messages.BorrowerData
import ru.itmo.messages.KAFKA_BORROWER_TOPIC
import ru.itmo.repository.Borrower
import ru.itmo.repository.BorrowerRepository

@Service
class KafkaBorrowerService(
    private val borrowerRepository: BorrowerRepository
) {
    private val log: Logger = LoggerFactory.getLogger(KafkaBorrowerService::class.java)

    @KafkaListener(id = "Borrower", topics = [KAFKA_BORROWER_TOPIC], containerFactory = "singleFactory")
    fun consumeBorrower(dto: BorrowerData) {
        log.info("kafka borrower consumer $dto")
        val borrower = Borrower(dto.id, dto.firstName, dto.lastName, dto.passportSeriesAndNumber)
        borrowerRepository.save(borrower)
    }

}