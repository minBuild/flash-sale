package dev.minbuild.flashsale.application.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.Acknowledgment

class OrderEventConsumerTest {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var acknowledgment: Acknowledgment
    private lateinit var orderEventConsumer: OrderEventConsumer

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        acknowledgment = mockk(relaxed = true)
        orderEventConsumer = OrderEventConsumer(objectMapper)
    }

    @Test
    @DisplayName("정상적인 JSON 페이로드가 들어오면 처리를 완료하고 acknowledge를 호출한다.")
    fun consumeOrderCreatedEvent_Success() = runBlocking {
        // given
        val validPayload = """{"orderId": 100, "userId": 1, "aggregate_type": "ORDER"}"""

        // when
        orderEventConsumer.consumeOrderCreatedEvent(validPayload, acknowledgment)

        // then
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
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

}
