package dev.minbuild.flashsale.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청"),

    // FlashSale
    PARTICIPATION_REJECTED(HttpStatus.CONFLICT, "FS001", "선착순이 마감되었거나 이미 참여한 유저입니다."),
    ORDER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FS002", "주문 생성 및 식별자 발급에 실패했습니다."),

}
