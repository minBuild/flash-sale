package dev.minbuild.flashsale.common.exception

abstract class FlashSaleException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)

class FlashSaleRejectedException : FlashSaleException(ErrorCode.PARTICIPATION_REJECTED)

class OrderCreationFailedException : FlashSaleException(ErrorCode.ORDER_CREATION_FAILED)
