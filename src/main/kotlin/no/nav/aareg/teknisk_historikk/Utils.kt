package no.nav.aareg.teknisk_historikk

import jakarta.servlet.http.HttpServletRequest
import no.nav.aareg.teknisk_historikk.models.TjenestefeilResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity.status

fun tjenestefeilRespons(httpServletRequest: HttpServletRequest, httpStatus: HttpStatus, vararg feilmeldinger: String) =
    tjenestefeilRespons(httpServletRequest, httpStatus, feilmeldinger.asList())

fun tjenestefeilRespons(httpServletRequest: HttpServletRequest, httpStatus: HttpStatus, feilmeldinger: List<String>) =
    status(httpStatus).body(TjenestefeilResponse().apply {
        korrelasjonsid = httpServletRequest.getAttribute(KORRELASJONSID_HEADER) as String
        meldinger = feilmeldinger
    })
