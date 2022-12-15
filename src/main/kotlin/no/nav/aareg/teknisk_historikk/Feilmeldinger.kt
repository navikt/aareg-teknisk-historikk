package no.nav.aareg.teknisk_historikk

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class Feilmeldinger {
    @ExceptionHandler(Exception::class)
    fun handler(exception: Throwable, httpServletRequest: HttpServletRequest): ResponseEntity<String> {
        return status(HttpStatus.INTERNAL_SERVER_ERROR).body("En ukjent feil oppstod")
    }
}