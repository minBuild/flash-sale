package dev.minbuild.flashsale.application

import com.fasterxml.jackson.databind.ObjectMapper
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

        val isWinner = flashSaleRedisRepository.attemptToParticipate(userId, productId)

        if (!isWinner) {
            throw IllegalStateException("선착순이 마감되었거나 이미 참여한 유저입니다.")
        }

        val order = Order(userId = userId, productId = productId)
        val savedOrder = orderRepository.save(order)
        val orderId = savedOrder.id ?: throw IllegalStateException("주문 생성 실패: ID가 없습니다.")

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
