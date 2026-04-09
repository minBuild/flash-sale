package dev.minbuild.flashsale.infrastructure.redis

interface FlashSaleRedisRepository {
    suspend fun attemptToParticipate(userId: Long, productId: Long): Boolean
}
