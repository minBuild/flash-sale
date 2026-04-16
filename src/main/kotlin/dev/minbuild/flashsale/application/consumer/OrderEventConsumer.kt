package dev.minbuild.flashsale.application.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import dev.minbuild.flashsale.application.client.OrderApiClient
import dev.minbuild.flashsale.common.utils.log
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class OrderEventConsumer(
    private val objectMapper: ObjectMapper,
    private val orderApiClient: OrderApiClient,
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    @KafkaListener(
        topics = ["order-created-topic"],
        groupId = "flash-sale-order-group"
    )
    suspend fun consumeOrderCreatedEvent(payload: String, acknowledgment: Acknowledgment) {
        log.info("[Kafka Event Received] Payload: {}", payload)

        val orderId: Long
        val userId: Long

        try {
            val eventData = objectMapper.readValue(payload, Map::class.java)
            orderId = eventData["orderId"]?.toString()?.toLongOrNull()
                ?: throw IllegalArgumentException("OrderId is missing")
            userId = eventData["userId"]?.toString()?.toLongOrNull()
                ?: throw IllegalArgumentException("UserId is missing")
        } catch (e: Exception) {
            log.error("Invalid payload detected. Payload: {}", payload, e)
            sendToDlq(payload, e.message ?: "Unknown Parsing Error")

            acknowledgment.acknowledge()
            return
        }

        val idempotencyKey = "flash_sale:idempotency:order:$orderId"

        try {
            val isProcessed = redisTemplate.hasKey(idempotencyKey).awaitSingleOrNull() == true

            if (isProcessed) {
                log.warn(
                    "Duplicate API call detected and skipped. Order is already processed. (OrderId: {})",
                    orderId
                )
                acknowledgment.acknowledge()
                return
            }

            orderApiClient.requestOrder(orderId, userId)

            redisTemplate.opsForValue()
                .set(idempotencyKey, "DONE", Duration.ofDays(7))
                .awaitSingleOrNull()

            acknowledgment.acknowledge()
            log.info(
                "Successfully requested to Order System and acknowledged (OrderId: {})",
                orderId
            )

        } catch (e: Exception) {
            log.error("API call failed. Will wait for Kafka retry. Payload: {}", payload, e)
        }
    }

    private suspend fun sendToDlq(originalPayload: String, errorMessage: String) {
        val dlqTopic = "order-created-dlq"
        try {
            val record = ProducerRecord<String, String>(dlqTopic, originalPayload)
            record.headers().add("X-Error-Reason", errorMessage.toByteArray())

            kafkaTemplate.send(record).await()
            log.info("Successfully routed Poison Pill to DLQ Topic: {}", dlqTopic)
        } catch (e: Exception) {
            log.error("Failed to send message to DLQ. Payload: {}", originalPayload, e)
        }
    }

}
