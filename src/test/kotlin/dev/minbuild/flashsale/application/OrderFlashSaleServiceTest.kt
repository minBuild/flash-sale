package dev.minbuild.flashsale.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.minbuild.flashsale.domain.order.Order
import dev.minbuild.flashsale.domain.order.OrderRepository
import dev.minbuild.flashsale.domain.outbox.OutboxEvent
import dev.minbuild.flashsale.domain.outbox.OutboxEventRepository
import dev.minbuild.flashsale.infrastructure.redis.FlashSaleRedisRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class OrderFlashSaleServiceTest {

    private val flashSaleRedisRepository: FlashSaleRedisRepository = mockk()
    private val orderRepository: OrderRepository = mockk()
    private val outboxEventRepository: OutboxEventRepository = mockk()
    private val objectMapper = jacksonObjectMapper()

    private val orderFlashSaleService = OrderFlashSaleService(
        flashSaleRedisRepository,
        orderRepository,
        outboxEventRepository,
        objectMapper
    )

    @Test
    @DisplayName("선착순 조건에 부합하면 주문과 아웃박스 이벤트가 성공적으로 저장된다.")
    fun placeOrder_success() = runTest {
        // given
        val userId = 1L
        val productId = 100L
        val expectedOrderId = 10L

        coEvery { flashSaleRedisRepository.attemptToParticipate(userId, productId) } returns true
        coEvery { orderRepository.save(any<Order>()) } returns Order(
            id = expectedOrderId,
            userId = userId,
            productId = productId
        )
        coEvery { outboxEventRepository.save(any<OutboxEvent>()) } returns mockk()

        // when
        val result = orderFlashSaleService.placeOrder(userId, productId)

        // then
        assertNotNull(result.id)
        assertEquals(expectedOrderId, result.id)
        coVerify(exactly = 1) { orderRepository.save(any<Order>()) }
        coVerify(exactly = 1) { outboxEventRepository.save(any<OutboxEvent>()) }
    }

    @Test
    @DisplayName("레디스 선착순 검증에서 실패하면 예외가 발생하고 DB 저장은 수행되지 않는다.")
    fun placeOrder_fail_whenRedisRejects() = runTest {
        // given
        val userId = 1L
        val productId = 100L

        coEvery { flashSaleRedisRepository.attemptToParticipate(userId, productId) } returns false

        // when & then
        val exception = assertThrows<IllegalStateException> {
            orderFlashSaleService.placeOrder(userId, productId)
        }
        assertEquals("선착순이 마감되었거나 이미 참여한 유저입니다.", exception.message)

        coVerify(exactly = 0) { orderRepository.save(any()) }
        coVerify(exactly = 0) { outboxEventRepository.save(any()) }
    }

    @Test
    @DisplayName("DB 저장 후 주문 ID가 null이면 예외가 발생한다")
    fun placeOrder_fail_whenSavedOrderIdIsNull() = runTest {
        // given
        val userId = 1L
        val productId = 100L

        coEvery { flashSaleRedisRepository.attemptToParticipate(userId, productId) } returns true
        coEvery { orderRepository.save(any<Order>()) } returns Order(
            id = null,
            userId = userId,
            productId = productId
        )

        // when & then
        val exception = assertThrows<IllegalStateException> {
            orderFlashSaleService.placeOrder(userId, productId)
        }
        assertEquals("주문 생성 실패: ID가 없습니다.", exception.message)

        coVerify(exactly = 0) { outboxEventRepository.save(any()) }
    }

}
