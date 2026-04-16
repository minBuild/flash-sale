package dev.minbuild.flashsale.application.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import dev.minbuild.flashsale.application.client.OrderApiClient
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.kafka.support.Acknowledgment
import reactor.core.publisher.Mono
import java.time.Duration

class OrderEventConsumerTest {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var acknowledgment: Acknowledgment
    private lateinit var orderEventConsumer: OrderEventConsumer
    private lateinit var orderApiClient: OrderApiClient
    private lateinit var redisTemplate: ReactiveStringRedisTemplate
    private lateinit var valueOperations: ReactiveValueOperations<String, String>

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        acknowledgment = mockk(relaxed = true)
        orderApiClient = mockk()
        redisTemplate = mockk()
        valueOperations = mockk()

        every { redisTemplate.opsForValue() } returns valueOperations

        orderEventConsumer = OrderEventConsumer(
            objectMapper = objectMapper,
            orderApiClient = orderApiClient,
            redisTemplate = redisTemplate
        )
    }

    @Test
    @DisplayName("정상적인 페이로드이고 처음 들어온 주문이면 API 호출 후 Redis에 저장하고 acknowledge를 호출한다.")
    fun consumeOrderCreatedEvent_Success() = runBlocking {
        // given
        val validPayload = """{"orderId": 100, "userId": 1, "aggregate_type": "ORDER"}"""

        every { redisTemplate.hasKey("flash_sale:idempotency:order:100") } returns Mono.just(false)
        coEvery { orderApiClient.requestOrder(100L, 1L) } just Runs
        every { valueOperations.set(any(), "DONE", any<Duration>()) } returns Mono.just(true)

        // when
        orderEventConsumer.consumeOrderCreatedEvent(validPayload, acknowledgment)

        // then
        coVerify(exactly = 1) { orderApiClient.requestOrder(100L, 1L) }
        verify(exactly = 1) { valueOperations.set(any(), "DONE", any<Duration>()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    @DisplayName("orderId가 누락된 불량 페이로드가 들어오면 독약(Poison Pill)으로 간주하고, 스킵을 위해 acknowledge를 호출한다.")
    fun consumeOrderCreatedEvent_MissingOrderId_PoisonPill() = runBlocking {
        // given
        val invalidPayload = """{"userId": 1}"""

        // when
        orderEventConsumer.consumeOrderCreatedEvent(invalidPayload, acknowledgment)

        // then
        coVerify(exactly = 0) { orderApiClient.requestOrder(any(), any()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    @DisplayName("JSON 형식이 깨진 페이로드가 들어오면 독약(Poison Pill)으로 간주하고, 스킵을 위해 acknowledge를 호출한다.")
    fun consumeOrderCreatedEvent_InvalidJson_PoisonPill() = runBlocking {
        // given
        val brokenPayload = """{orderId: 100"""

        // when
        orderEventConsumer.consumeOrderCreatedEvent(brokenPayload, acknowledgment)

        // then
        coVerify(exactly = 0) { orderApiClient.requestOrder(any(), any()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    @DisplayName("이미 처리된 주문(멱등성 키 존재)이면 API를 호출하지 않고, 스킵을 위해 acknowledge를 호출한다.")
    fun consumeOrderCreatedEvent_Duplicate_Idempotency() = runBlocking {
        // given
        val validPayload = """{"orderId": 100, "userId": 1, "aggregate_type": "ORDER"}"""

        every { redisTemplate.hasKey("flash_sale:idempotency:order:100") } returns Mono.just(true)

        // when
        orderEventConsumer.consumeOrderCreatedEvent(validPayload, acknowledgment)

        // then
        coVerify(exactly = 0) { orderApiClient.requestOrder(any(), any()) }
        verify(exactly = 0) { valueOperations.set(any(), any(), any<Duration>()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    @DisplayName("외부 API 호출 중 에러가 발생하면 재시도를 위해 acknowledge를 호출하지 않는다.")
    fun consumeOrderCreatedEvent_ApiCallFailed() = runBlocking {
        // given
        val validPayload = """{"orderId": 100, "userId": 1, "aggregate_type": "ORDER"}"""

        every { redisTemplate.hasKey(any()) } returns Mono.just(false)
        coEvery {
            orderApiClient.requestOrder(
                any(),
                any()
            )
        } throws RuntimeException("API Server Down")

        // when
        orderEventConsumer.consumeOrderCreatedEvent(validPayload, acknowledgment)

        // then
        verify(exactly = 0) { valueOperations.set(any(), any(), any<Duration>()) }
        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

}
