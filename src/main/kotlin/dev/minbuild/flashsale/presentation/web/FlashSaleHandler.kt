package dev.minbuild.flashsale.presentation.web

import dev.minbuild.flashsale.application.OrderFlashSaleService
import dev.minbuild.flashsale.presentation.dto.FlashSaleRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Component
class FlashSaleHandler(
    private val orderFlashSaleService: OrderFlashSaleService
) {
    suspend fun placeOrder(request: ServerRequest): ServerResponse {
        val flashSaleRequest = request.awaitBody<FlashSaleRequest>()

        val order = orderFlashSaleService.placeOrder(
            userId = flashSaleRequest.userId,
            productId = flashSaleRequest.productId
        )

        return ServerResponse.ok().bodyValueAndAwait(order)
    }

}
