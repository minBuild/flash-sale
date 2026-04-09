package dev.minbuild.flashsale.domain.order

interface OrderRepository {
    suspend fun save(order: Order): Order
    suspend fun findById(id: Long): Order?
}
