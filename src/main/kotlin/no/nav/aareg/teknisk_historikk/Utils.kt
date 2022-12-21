package no.nav.aareg.teknisk_historikk

import no.nav.aareg.teknisk_historikk.models.TjenestefeilResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity.status

fun tjenestefeilRespons(httpStatus: HttpStatus, vararg feilmeldinger: String) =
    tjenestefeilRespons(httpStatus, feilmeldinger.asList())

fun tjenestefeilRespons(httpStatus: HttpStatus, feilmeldinger: List<String>) =
    status(httpStatus).body(TjenestefeilResponse().apply {
        meldinger = feilmeldinger
    })