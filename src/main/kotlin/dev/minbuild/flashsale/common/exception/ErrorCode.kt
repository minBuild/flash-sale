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
    SOLD_OUT(HttpStatus.CONFLICT, "FS001", "선착순 재고가 모두 소진되었습니다."),
    DUPLICATED_PARTICIPATION(HttpStatus.CONFLICT, "FS002", "이미 이벤트에 참여한 유저입니다."),
    ORDER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FS003", "주문 생성 및 식별자 발급에 실패했습니다."),

}
