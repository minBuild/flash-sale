package dev.minbuild.flashsale.presentation.web

import dev.minbuild.flashsale.application.OrderFlashSaleService
import dev.minbuild.flashsale.domain.order.Order
import dev.minbuild.flashsale.domain.order.OrderStatus
import dev.minbuild.flashsale.presentation.dto.FlashSaleRequest
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

internal class FlashSaleHandlerTest {

    private val orderFlashSaleService: OrderFlashSaleService = mockk()
    private val handler = FlashSaleHandler(orderFlashSaleService)
    private val router = FlashSaleRouter().flashSaleRoutes(handler)
    private val webTestClient = WebTestClient.bindToRouterFunction(router).build()

    @Test
    @DisplayName("POST /api/v1/flash-sales 호출 시 정상적으로 주문이 생성되고 200 OK를 반환한다.")
    fun placeOrder_success() {
        // given
        val request = FlashSaleRequest(userId = 1L, productId = 100L)
        val mockOrder = Order(
            id = 10L,
            userId = 1L,
            productId = 100L,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )

        coEvery { orderFlashSaleService.placeOrder(1L, 100L) } returns mockOrder

        // when & then
        webTestClient.post()
            .uri("/api/v1/flash-sales")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(10L)
            .jsonPath("$.userId").isEqualTo(1L)
            .jsonPath("$.productId").isEqualTo(100L)
            .jsonPath("$.status").isEqualTo("PENDING")
    }

}
