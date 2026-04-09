package dev.minbuild.flashsale.common.exception

import dev.minbuild.flashsale.common.utils.log
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(FlashSaleException::class)
    fun handleFlashSaleException(e: FlashSaleException): ResponseEntity<ErrorResponse> {
        log.warn("Business Exception: ${e.errorCode.message}", e)

        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.from(e.errorCode))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled Exception: ${e.message}", e)

        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR))
    }

}
