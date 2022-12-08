package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonProperty

data class Bruksperiode(
    @JsonProperty(value = "fom", required = true)
    var fom: String,
    @JsonProperty(value = "tom", required = false)
    var tom: String?
)