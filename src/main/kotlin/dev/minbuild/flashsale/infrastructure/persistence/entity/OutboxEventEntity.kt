package dev.minbuild.flashsale.infrastructure.persistence.entity

import dev.minbuild.flashsale.domain.outbox.OutboxEvent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("outbox_event")
data class OutboxEventEntity(
    @Id
    val id: Long? = null,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val createdAt: LocalDateTime
) {
    fun toDomain(): OutboxEvent {
        return OutboxEvent(
            id = id,
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            eventType = eventType,
            payload = payload,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(domain: OutboxEvent): OutboxEventEntity {
            return OutboxEventEntity(
                aggregateType = domain.aggregateType,
                aggregateId = domain.aggregateId,
                eventType = domain.eventType,
                payload = domain.payload,
                createdAt = domain.createdAt
            )
        }
    }
    
}
