package dev.minbuild.flashsale.domain.outbox

import java.time.LocalDateTime

class OutboxEvent(
    val id: Long? = null,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
