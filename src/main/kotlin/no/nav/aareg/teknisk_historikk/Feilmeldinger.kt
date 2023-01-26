package no.nav.aareg.teknisk_historikk

import jakarta.servlet.http.HttpServletRequest
import no.nav.aareg.teknisk_historikk.models.TjenestefeilResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class Feilmeldinger {

    private val log = LoggerFactory.getLogger(Feilmeldinger::class.java)
    private val secureLog = LoggerFactory.getLogger("secureLogger")

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun feilMediaType(exception: HttpMediaTypeNotSupportedException, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(
            httpServletRequest,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Feil mediatype: ${httpServletRequest.contentType}",
            "Støttede typer: ${exception.supportedMediaTypes.joinToString(", ")}"
        )

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun feilRequestType(exception: HttpRequestMethodNotSupportedException, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(
            httpServletRequest,
            HttpStatus.METHOD_NOT_ALLOWED,
            "Http-verb ikke tillatt: ${httpServletRequest.method}",
            "Verb som er støttet: ${exception.supportedHttpMethods?.joinToString(", ")}"
        )

    @ExceptionHandler(Exception::class)
    fun generiskFeil(exception: Throwable, httpServletRequest: HttpServletRequest): ResponseEntity<TjenestefeilResponse> {
        log.error("Uhåndtert feil oppstod. Sjekk sikker log for detaljer")
        secureLog.error("Uhåndtert feil oppstod", exception)
        return tjenestefeilRespons(httpServletRequest, HttpStatus.INTERNAL_SERVER_ERROR, "En ukjent feil oppstod")
    }
}