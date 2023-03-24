package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aareg.teknisk_historikk.models.Kodeverksentitet
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Ansettelsesperiode(
    val startdato: LocalDate,
    @JsonProperty(required = false)
    val sluttdato: LocalDate?,
    @JsonProperty(required = false)
    val sluttaarsak: Kodeverksentitet?,
    @JsonProperty(required = false)
    val bruksperiode: Bruksperiode?,
    val sporingsinformasjon: Sporingsinformasjon
)