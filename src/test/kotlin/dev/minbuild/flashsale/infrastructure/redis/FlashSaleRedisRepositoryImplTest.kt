package dev.minbuild.flashsale.infrastructure.redis

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import reactor.core.publisher.Flux

internal class FlashSaleRedisRepositoryImplTest {

    private val redisTemplate: ReactiveStringRedisTemplate = mockk()
    private val repository = FlashSaleRedisRepositoryImpl(redisTemplate)

    @Test
    @DisplayName("Lua 스크립트가 1을 반환하면 SUCCESS 상태를 반환한다.")
    fun return_success() = runTest {
        // given
        every {
            redisTemplate.execute(
                any<RedisScript<Long>>(),
                any<List<String>>(),
                any<List<String>>()
            )
        } returns Flux.just(1L)

        // when
        val result = repository.attemptToParticipate(1L, 100L)

        // then
        assertTrue(result)
    }

    @Test
    @DisplayName("Lua 스크립트가 2를 반환하면 SOLD_OUT 상태를 반환한다.")
    fun return_sold_out() = runTest {
        // given
        every {
            redisTemplate.execute(
                any<RedisScript<Long>>(),
                any<List<String>>(),
                any<List<String>>()
            )
        } returns Flux.just(2L)

        // when
        val result = repository.attemptToParticipate(2L, 100L)

        // then
        assertFalse(result)
    }

    @Test
    @DisplayName("Lua 스크립트가 3을 반환하면 DUPLICATED 상태를 반환한다.")
    fun return_duplicated() = runTest {
        // given
        every {
            redisTemplate.execute(
                any<RedisScript<Long>>(),
                any<List<String>>(),
                any<List<String>>()
            )
        } returns Flux.just(3L)

        // when
        val result = repository.attemptToParticipate(3L, 100L)

        // then
        assertFalse(result)
    }

}
