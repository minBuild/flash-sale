package dev.minbuild.flashsale.infrastructure.redis

import dev.minbuild.flashsale.common.utils.log
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

    override suspend fun attemptToParticipate(userId: Long, productId: Long): Boolean {
        val stockKey = "flashsale:stock:$productId"
        val usersKey = "flashsale:users:$productId"

        val result = redisTemplate.execute(
            flashSaleScript,
            listOf(stockKey, usersKey),
            listOf(userId.toString())
        ).awaitFirstOrNull()

        return when (result) {
            1L -> {
                log.info("선착순 성공! (userId: $userId, productId: $productId)")
                true
            }

            2L -> {
                log.warn("선착순 실패: 재고 소진 (userId: $userId, productId: $productId)")
                false
            }

            3L -> {
                log.warn("선착순 실패: 중복 참여 (userId: $userId, productId: $productId)")
                false
            }

            else -> {
                log.error("Redis Lua Script 실행 중 알 수 없는 에러 발생: $result")
                false
            }
        }
    }

}
