package dev.minbuild.flashsale.common.exception

abstract class FlashSaleException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)

class FlashSaleSoldOutException : FlashSaleException(ErrorCode.SOLD_OUT)

class FlashSaleDuplicatedException : FlashSaleException(ErrorCode.DUPLICATED_PARTICIPATION)

class OrderCreationFailedException : FlashSaleException(ErrorCode.ORDER_CREATION_FAILED)
