package dev.minbuild.flashsale.application.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import dev.minbuild.flashsale.application.client.OrderApiClient
import dev.minbuild.flashsale.common.utils.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val objectMapper: ObjectMapper,
    private val orderApiClient: OrderApiClient
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
            acknowledgment.acknowledge()
            return
        }

        try {
            orderApiClient.requestOrder(orderId, userId)

            acknowledgment.acknowledge()
            log.info(
                "Successfully requested to Order System and acknowledged (OrderId: {})",
                orderId
            )

        } catch (e: Exception) {
            log.error("API call failed. Will wait for Kafka retry. Payload: {}", payload, e)
        }
    }

}
