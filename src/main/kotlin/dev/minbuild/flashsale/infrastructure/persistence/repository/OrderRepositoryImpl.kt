package dev.minbuild.flashsale.infrastructure.persistence.repository

import dev.minbuild.flashsale.domain.order.Order
import dev.minbuild.flashsale.domain.order.OrderRepository
import dev.minbuild.flashsale.infrastructure.persistence.entity.OrderEntity
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryImpl(
    private val r2dbcRepository: R2dbcOrderRepository
) : OrderRepository {

    override suspend fun save(order: Order): Order {
        val entity = OrderEntity.fromDomain(order)
        val savedEntity = r2dbcRepository.save(entity)
        return savedEntity.toDomain()
    }

    override suspend fun findById(id: Long): Order? {
        val entity = r2dbcRepository.findById(id)
        return entity?.toDomain()
    }

}
