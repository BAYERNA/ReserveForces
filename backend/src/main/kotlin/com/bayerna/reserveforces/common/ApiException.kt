package com.bayerna.reserveforces.common

import org.springframework.http.HttpStatus

class ApiException(val status: HttpStatus, message: String) : RuntimeException(message) {
    companion object {
        fun notFound(message: String) = ApiException(HttpStatus.NOT_FOUND, message)
        fun badRequest(message: String) = ApiException(HttpStatus.BAD_REQUEST, message)
        fun unauthorized(message: String) = ApiException(HttpStatus.UNAUTHORIZED, message)
        fun forbidden(message: String) = ApiException(HttpStatus.FORBIDDEN, message)
        fun conflict(message: String) = ApiException(HttpStatus.CONFLICT, message)
    }
}
