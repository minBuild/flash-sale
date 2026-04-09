package dev.minbuild.flashsale.common.exception

data class ErrorResponse(
    val code: String,
    val message: String
) {
    companion object {
        fun from(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = errorCode.message
            )
        }
    }

}
