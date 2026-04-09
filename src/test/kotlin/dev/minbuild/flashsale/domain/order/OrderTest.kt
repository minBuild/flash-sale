package dev.minbuild.flashsale.domain.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class OrderTest {

    @Test
    @DisplayName("새로운 주문을 생성하면 초기 상태는 PENDING이어야 한다.")
    fun createOrder_initialStatus_isPending() {
        // given & when
        val order = Order(userId = 1L, productId = 100L)

        // then
        assertEquals(OrderStatus.PENDING, order.status)
    }

    @Test
    @DisplayName("주문을 완료 처리하면 상태가 SUCCESS로 변경된다.")
    fun completeOrder_changesStatus_toSuccess() {
        // given
        val order = Order(userId = 1L, productId = 100L)

        // when
        order.complete()

        // then
        assertEquals(OrderStatus.SUCCESS, order.status)
    }

    @Test
    @DisplayName("주문을 실패 처리하면 상태가 FAILED로 변경된다.")
    fun failOrder_changesStatus_toFailed() {
        // given
        val order = Order(userId = 1L, productId = 100L)

        // when
        order.fail()

        // then
        assertEquals(OrderStatus.FAILED, order.status)
    }

}
