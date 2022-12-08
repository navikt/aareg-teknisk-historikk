package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sporingsinformasjon(
    @JsonProperty(value = "opprettetTidspunkt", required = true)
    var opprettetTidspunkt: LocalDateTime,
    @JsonProperty(value = "endretTidspunkt", required = true)
    var endretTidspunkt: LocalDateTime,
    @JsonProperty(value = "opprettetKilde", required = true)
    var opprettetKilde: String,
    @JsonProperty(value = "endretKilde", required = true)
    var endretKilde: String,
)