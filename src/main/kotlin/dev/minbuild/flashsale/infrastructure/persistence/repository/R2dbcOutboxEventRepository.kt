package dev.minbuild.flashsale.infrastructure.persistence.repository

import dev.minbuild.flashsale.infrastructure.persistence.entity.OutboxEventEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface R2dbcOutboxEventRepository : CoroutineCrudRepository<OutboxEventEntity, Long>
