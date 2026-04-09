package dev.minbuild.flashsale.infrastructure.persistence.repository

import dev.minbuild.flashsale.domain.order.Order
import dev.minbuild.flashsale.domain.order.OrderStatus
import dev.minbuild.flashsale.infrastructure.persistence.entity.OrderEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class OrderRepositoryImplTest {

    private val r2dbcRepository: R2dbcOrderRepository = mockk()
    private val orderRepository = OrderRepositoryImpl(r2dbcRepository)

    @Test
    @DisplayName("도메인 Order를 저장하면 Entity로 변환되어 DB에 저장되고 다시 도메인으로 반환된다.")
    fun saveOrder_mappingsWorkCorrectly() = runTest {
        // given
        val domainOrder = Order(userId = 1L, productId = 100L)
        val savedEntity = OrderEntity(
            id = 1L,
            userId = 1L,
            productId = 100L,
            status = OrderStatus.PENDING.name,
            createdAt = LocalDateTime.now()
        )

        coEvery { r2dbcRepository.save(any<OrderEntity>()) } returns savedEntity

        // when
        val resultOrder = orderRepository.save(domainOrder)

        // then
        assertNotNull(resultOrder.id)
        assertEquals(1L, resultOrder.id)
        assertEquals(1L, resultOrder.userId)
        assertEquals(OrderStatus.PENDING, resultOrder.status)
    }

}
