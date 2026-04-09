package dev.minbuild.flashsale.domain.outbox

data class OrderCreatedEventPayload(
    val orderId: Long,
    val userId: Long,
    val productId: Long
)
