package dev.minbuild.flashsale.domain.outbox

interface OutboxEventRepository {
    suspend fun save(event: OutboxEvent): OutboxEvent
}
