package no.nav.aareg.teknisk_historikk

import no.nav.aareg.teknisk_historikk.aareg_services.AaregServicesForbiddenException
import no.nav.aareg.teknisk_historikk.models.TjenestefeilResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.Forbidden
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class Feilmeldinger {

    @ExceptionHandler(AaregServicesForbiddenException::class)
    fun forbiddenHandler(forbidden: Forbidden, httpServletRequest: HttpServletRequest): ResponseEntity<TjenestefeilResponse> {
        return status(HttpStatus.FORBIDDEN).body(
            TjenestefeilResponse().apply { meldinger = listOf("Du mangler tilgang til å gjøre oppslag på arbeidstakeren") }
        )
    }

    @ExceptionHandler(Feil::class)
    fun feilhandler(feil: Feil, httpServletRequest: HttpServletRequest): ResponseEntity<Feilrespons> {
        return status(HttpStatus.INTERNAL_SERVER_ERROR).body(feil.feilrespons())
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun feilMediaType(exception: HttpMediaTypeNotSupportedException, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Feil mediatype: ${httpServletRequest.contentType}")

    @ExceptionHandler(Exception::class)
    fun generiskFeil(exception: Throwable, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(HttpStatus.INTERNAL_SERVER_ERROR, "En ukjent feil oppstod")
}