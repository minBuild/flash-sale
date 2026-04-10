package dev.minbuild.flashsale.infrastructure.redis

import dev.minbuild.flashsale.domain.order.FlashSaleResult

interface FlashSaleRedisRepository {
    suspend fun attemptToParticipate(userId: Long, productId: Long): FlashSaleResult
}
