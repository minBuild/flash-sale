package dev.minbuild.flashsale.infrastructure.persistence.entity

import dev.minbuild.flashsale.domain.order.Order
import dev.minbuild.flashsale.domain.order.OrderStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("orders")
data class OrderEntity(
    @Id val id: Long? = null,
    val userId: Long,
    val productId: Long,
    val status: String,
    val createdAt: LocalDateTime
) {
    fun toDomain(): Order = Order(
        id = this.id,
        userId = this.userId,
        productId = this.productId,
        status = OrderStatus.valueOf(this.status),
        createdAt = this.createdAt
    )

    companion object {
        fun fromDomain(order: Order): OrderEntity = OrderEntity(
            id = order.id,
            userId = order.userId,
            productId = order.productId,
            status = order.status.name,
            createdAt = order.createdAt
        )
    }

}
