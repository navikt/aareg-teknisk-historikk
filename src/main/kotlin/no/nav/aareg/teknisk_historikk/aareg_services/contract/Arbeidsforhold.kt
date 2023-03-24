package no.nav.aareg.teknisk_historikk.aareg_services.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aareg.teknisk_historikk.models.Kodeverksentitet

@JsonIgnoreProperties(ignoreUnknown = true)
data class Arbeidsforhold(
    val id: String?,
    val navUuid: String,
    val type: Kodeverksentitet = Kodeverksentitet(),
    val arbeidstaker: Identobjekt,
    val arbeidssted: Identobjekt,
    val opplysningspliktig: Identobjekt,
    @JsonProperty(required = false)
    val ansettelsesperioder: List<Ansettelsesperiode>,
    val rapporteringsordning: Kodeverksentitet,
    val sporingsinformasjon: Sporingsinformasjon,
    val bruksperiode: Bruksperiode
)