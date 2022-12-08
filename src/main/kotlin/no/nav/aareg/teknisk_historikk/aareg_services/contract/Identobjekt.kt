package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class Identobjekt(
    @JsonProperty(value = "type", required = false)
    var type: String?,
    @JsonProperty(value = "identer", required = true)
    var identer: List<Ident>
)

data class Ident(
    @JsonProperty(value = "type", required = true)
    var type: String,
    @JsonProperty(value = "ident", required = true)
    var ident: String,
    @JsonProperty(value = "gjeldende", required = false)
    var gjeldende: Boolean
)