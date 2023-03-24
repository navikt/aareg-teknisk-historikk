package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class Identobjekt(
    @JsonProperty(required = false)
    val type: String?,
    val identer: List<Ident>
)

data class Ident(
    val type: String,
    val ident: String,
    @JsonProperty(required = false)
    val gjeldende: Boolean?
)