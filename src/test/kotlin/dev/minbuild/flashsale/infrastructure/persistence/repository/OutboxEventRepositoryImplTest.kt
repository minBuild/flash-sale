package dev.minbuild.flashsale.infrastructure.persistence.repository

import dev.minbuild.flashsale.domain.outbox.OutboxEvent
import dev.minbuild.flashsale.infrastructure.persistence.entity.OutboxEventEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class OutboxEventRepositoryImplTest {

    private val r2dbcRepository: R2dbcOutboxEventRepository = mockk()
    private val outboxEventRepository = OutboxEventRepositoryImpl(r2dbcRepository)

    @Test
    @DisplayName("도메인 OutboxEvent를 저장하면 Entity로 변환되어 저장되고 다시 도메인 객체로 반환된다.")
    fun save_success() = runTest {
        // given
        val now = LocalDateTime.now()
        val domainEvent = OutboxEvent(
            aggregateType = "ORDER",
            aggregateId = "1",
            eventType = "ORDER_CREATED",
            payload = """{"userId": 1, "productId": 100}""",
            createdAt = now
        )
        val savedEntity = OutboxEventEntity(
            id = 100L,
            aggregateType = "ORDER",
            aggregateId = "1",
            eventType = "ORDER_CREATED",
            payload = """{"userId": 1, "productId": 100}""",
            createdAt = now
        )

        coEvery { r2dbcRepository.save(any<OutboxEventEntity>()) } returns savedEntity

        // when
        val result = outboxEventRepository.save(domainEvent)

        // then
        assertNotNull(result.id)
        assertEquals(100L, result.id)
        assertEquals("ORDER", result.aggregateType)
        assertEquals("1", result.aggregateId)
        coVerify(exactly = 1) { r2dbcRepository.save(any<OutboxEventEntity>()) }
    }

}
