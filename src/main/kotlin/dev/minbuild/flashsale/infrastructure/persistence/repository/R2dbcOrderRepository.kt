package dev.minbuild.flashsale.infrastructure.persistence.repository

import dev.minbuild.flashsale.infrastructure.persistence.entity.OrderEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface R2dbcOrderRepository : CoroutineCrudRepository<OrderEntity, Long>
