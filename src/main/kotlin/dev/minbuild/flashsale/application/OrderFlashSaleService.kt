package dev.minbuild.flashsale.application

import com.fasterxml.jackson.databind.ObjectMapper
import dev.minbuild.flashsale.common.exception.FlashSaleDuplicatedException
import dev.minbuild.flashsale.common.exception.FlashSaleSoldOutException
import dev.minbuild.flashsale.common.exception.OrderCreationFailedException
import dev.minbuild.flashsale.domain.order.FlashSaleResult
import dev.minbuild.flashsale.domain.order.Order
import dev.minbuild.flashsale.domain.order.OrderRepository
import dev.minbuild.flashsale.domain.outbox.OrderCreatedEventPayload
import dev.minbuild.flashsale.domain.outbox.OutboxEvent
import dev.minbuild.flashsale.domain.outbox.OutboxEventRepository
import dev.minbuild.flashsale.infrastructure.redis.FlashSaleRedisRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderFlashSaleService(
    private val flashSaleRedisRepository: FlashSaleRedisRepository,
    private val orderRepository: OrderRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    suspend fun placeOrder(userId: Long, productId: Long): Order {

        val result = flashSaleRedisRepository.attemptToParticipate(userId, productId)

        when (result) {
            FlashSaleResult.SOLD_OUT -> throw FlashSaleSoldOutException()
            FlashSaleResult.DUPLICATED -> throw FlashSaleDuplicatedException()
            FlashSaleResult.SUCCESS -> {}
        }

        val order = Order(userId = userId, productId = productId)
        val savedOrder = orderRepository.save(order)
        val orderId = savedOrder.id ?: throw OrderCreationFailedException()

        val eventPayload = OrderCreatedEventPayload(
            orderId = orderId,
            userId = userId,
            productId = productId
        )
        val payloadJson = objectMapper.writeValueAsString(eventPayload)

        val outboxEvent = OutboxEvent(
            aggregateType = "ORDER",
            aggregateId = savedOrder.id.toString(),
            eventType = "ORDER_CREATED",
            payload = payloadJson
        )
        outboxEventRepository.save(outboxEvent)

        return savedOrder
    }

}
