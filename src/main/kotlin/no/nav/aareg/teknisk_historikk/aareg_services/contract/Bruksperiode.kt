package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonProperty

data class Bruksperiode(
    val fom: String,
    @JsonProperty(required = false)
    val tom: String?
)