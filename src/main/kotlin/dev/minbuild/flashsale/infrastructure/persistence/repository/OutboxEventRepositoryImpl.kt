package dev.minbuild.flashsale.infrastructure.persistence.repository

import dev.minbuild.flashsale.domain.outbox.OutboxEvent
import dev.minbuild.flashsale.domain.outbox.OutboxEventRepository
import dev.minbuild.flashsale.infrastructure.persistence.entity.OutboxEventEntity
import org.springframework.stereotype.Repository

@Repository
class OutboxEventRepositoryImpl(
    private val r2dbcRepository: R2dbcOutboxEventRepository
) : OutboxEventRepository {

    override suspend fun save(event: OutboxEvent): OutboxEvent {
        val entity = OutboxEventEntity.fromDomain(event)
        val savedEntity = r2dbcRepository.save(entity)
        return savedEntity.toDomain()
    }

}
