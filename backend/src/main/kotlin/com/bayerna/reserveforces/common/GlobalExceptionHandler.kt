package com.bayerna.reserveforces.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.OffsetDateTime

data class ApiError(val status: Int, val message: String, val timestamp: OffsetDateTime = OffsetDateTime.now())

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ApiError> =
        ResponseEntity.status(ex.status).body(ApiError(ex.status.value(), ex.message ?: "오류가 발생했습니다."))

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError(HttpStatus.FORBIDDEN.value(), "접근 권한이 없습니다."))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = ex.bindingResult.fieldErrors.firstOrNull()
            ?.let { "${it.field}: ${it.defaultMessage}" }
            ?: "요청 값이 올바르지 않습니다."
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(HttpStatus.BAD_REQUEST.value(), message))
    }

    /** 경로 변수 UUID 형식이 잘못된 경우 등 타입 변환 실패는 500이 아닌 400으로 응답한다. */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiError> {
        val message = "${ex.name} 값의 형식이 올바르지 않습니다: ${ex.value}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(HttpStatus.BAD_REQUEST.value(), message))
    }

    /** 요청 본문이 JSON으로 파싱되지 않는 경우(문법 오류 등) 500이 아닌 400으로 응답한다. */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(HttpStatus.BAD_REQUEST.value(), "요청 본문의 형식이 올바르지 않습니다."))

    /** 존재하지 않는 경로 요청은 500이 아닌 404로 응답한다. */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(ex: NoResourceFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError(HttpStatus.NOT_FOUND.value(), "요청한 API를 찾을 수 없습니다."))

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "예기치 못한 오류가 발생했습니다."))
}
