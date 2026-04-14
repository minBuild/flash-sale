package dev.minbuild.flashsale.application.client

import dev.minbuild.flashsale.common.utils.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class OrderApiClient(
    webClientBuilder: WebClient.Builder,
    @param:Value("\${external.api.order.url}")
    private val orderServerUrl: String
) {
    private val webClient = webClientBuilder.baseUrl(orderServerUrl).build()

    suspend fun requestOrder(orderId: Long, userId: Long) {
        log.info("External Order API call... (OrderId: {})", orderId)

        val requestBody = mapOf(
            "orderId" to orderId,
            "userId" to userId,
            "source" to "FLASH_SALE"
        )

        try {
            webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .awaitBody<String>()

            log.info("External Order API call successful! (OrderId: {})", orderId)
        } catch (e: Exception) {
            log.error("Failed to call External Order System. (OrderId: {})", orderId, e)
            throw e
        }
    }

}
