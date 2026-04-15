package dev.minbuild.flashsale.presentation.web

import dev.minbuild.flashsale.application.StockAdminService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Component
class AdminHandler(
    private val stockAdminService: StockAdminService
) {
    suspend fun setStock(request: ServerRequest): ServerResponse {
        val itemId = request.pathVariable("itemId").toLong()
        val body = request.awaitBody<StockSetRequest>()

        stockAdminService.setStock(itemId, body.stock)

        val responseBody = mapOf(
            "status" to "SUCCESS",
            "itemId" to itemId,
            "setStock" to body.stock
        )

        return ServerResponse.ok().bodyValueAndAwait(responseBody)
    }

}

data class StockSetRequest(val stock: Long)
