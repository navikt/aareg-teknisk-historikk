package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aareg.teknisk_historikk.models.Kodeverksentitet
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Ansettelsesperiode(
    @JsonProperty(value = "startdato", required = true)
    var startdato: LocalDate,
    @JsonProperty(value = "sluttdato", required = false)
    var sluttdato: LocalDate?,
    @JsonProperty(value = "sluttaarsak", required = false)
    var sluttaarsak: Kodeverksentitet?,
    @JsonProperty(value = "bruksperiode", required = false)
    var bruksperiode: Bruksperiode,
    @JsonProperty(value = "sporingsinformasjon", required = true)
    var sporingsinformasjon: Sporingsinformasjon
)