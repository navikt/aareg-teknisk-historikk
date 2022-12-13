package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aareg.teknisk_historikk.models.Kodeverksentitet

@JsonIgnoreProperties(ignoreUnknown = true)
data class Arbeidsforhold(
    @JsonProperty(value = "id", required = false)
    var id: String?,
    @JsonProperty(value = "navUuid", required = true)
    var navUuid: String,
    @JsonProperty(value = "type", required = true)
    var type: Kodeverksentitet = Kodeverksentitet(),
    @JsonProperty(value = "arbeidstaker", required = true)
    var arbeidstaker: Identobjekt,
    @JsonProperty(value = "arbeidssted", required = true)
    var arbeidssted: Identobjekt,
    @JsonProperty(value = "opplysningspliktig", required = true)
    var opplysningspliktig: Identobjekt,
    @JsonProperty(value = "ansettelsesperioder", required = false)
    var ansettelsesperioder: List<Ansettelsesperiode>,
    @JsonProperty(value = "rapporteringsordning", required = true)
    var rapporteringsordning: Kodeverksentitet,
    @JsonProperty(value = "sporingsinformasjon", required = true)
    var sporingsinformasjon: Sporingsinformasjon,
    @JsonProperty(value = "bruksperiode", required = true)
    var bruksperiode: Bruksperiode
)