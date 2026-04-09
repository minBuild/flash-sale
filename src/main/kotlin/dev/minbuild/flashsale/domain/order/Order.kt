package dev.minbuild.flashsale.domain.order

import java.time.LocalDateTime

class Order(
    val id: Long? = null,
    val userId: Long,
    val productId: Long,
    var status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun complete() {
        this.status = OrderStatus.SUCCESS
    }

    fun fail() {
        this.status = OrderStatus.FAILED
    }
}

enum class OrderStatus {
    PENDING, SUCCESS, FAILED
}
