package dev.minbuild.flashsale.infrastructure.redis

import dev.minbuild.flashsale.domain.order.FlashSaleResult
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository

@Repository
class FlashSaleRedisRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : FlashSaleRedisRepository {

    private val flashSaleScript = DefaultRedisScript<Long>().apply {
        setLocation(ClassPathResource("scripts/flash_sale.lua"))
        resultType = Long::class.java
    }

    override suspend fun attemptToParticipate(userId: Long, productId: Long): FlashSaleResult {
        val stockKey = "flashsale:stock:$productId"
        val usersKey = "flashsale:users:$productId"

        val result = redisTemplate.execute(
            flashSaleScript,
            listOf(stockKey, usersKey),
            listOf(userId.toString())
        ).awaitFirstOrNull()

        return when (result) {
            1L -> FlashSaleResult.SUCCESS
            2L -> FlashSaleResult.SOLD_OUT
            3L -> FlashSaleResult.DUPLICATED
            else -> throw IllegalStateException("Redis Lua Script 실행 중 알 수 없는 에러 발생: $result")
        }
    }

}
