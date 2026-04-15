package dev.minbuild.flashsale.application

import dev.minbuild.flashsale.common.utils.log
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service

@Service
class StockAdminService(
    private val redisTemplate: ReactiveStringRedisTemplate
) {
    suspend fun setStock(itemId: Long, stock: Long) {
        log.info("Setting stock for item {} to {}", itemId, stock)

        val key = "flashsale:stock:$itemId"

        redisTemplate.opsForValue().set(key, stock.toString()).awaitSingleOrNull()

        log.info("Redis stock initialized to Admin API. [Key: {}, Stock: {}]", key, stock)
    }

}
