package no.nav.aareg.teknisk_historikk.contract

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aareg.teknisk_historikk.models.Kodeverksentitet
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Arbeidsforhold(
    @JsonProperty(value = "id", required = false)
    var id: String?,
    @JsonProperty(value = "navUuid", required = true)
    var navUuid: String,
    @JsonProperty(value = "type", required = true)
    var type: Kodeverksentitet = Kodeverksentitet(),
    @JsonProperty(value = "arbeidstaker", required = true)
    var arbeidstaker: IdentContainer,
    @JsonProperty(value = "arbeidssted", required = true)
    var arbeidssted: IdentContainer,
    @JsonProperty(value = "opplysningspliktig", required = true)
    var opplysningspliktig: IdentContainer,
    @JsonProperty(value = "ansettelsesperioder", required = false)
    var ansettelsesperioder: List<AnsettelsesPeriode>?,
    @JsonProperty(value = "rapporteringsordning", required = true)
    var rapporteringsordning: Kodeverksentitet,
    @JsonProperty(value = "sporingsinformasjon", required = true)
    var sporingsinformasjon: Sporingsinformasjon,
    @JsonProperty(value = "bruksperiode", required = true)
    var bruksperiode: Bruksperiode
)

data class Bruksperiode(
    @JsonProperty(value = "fom", required = true)
    var fom: String,
    @JsonProperty(value = "tom", required = false)
    var tom: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class IdentContainer(
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnsettelsesPeriode(
    @JsonProperty(value = "startdato", required = true)
    var startdato: LocalDate,
    @JsonProperty(value = "sluttdato", required = false)
    var sluttdato: LocalDate?,
    @JsonProperty(value = "sluttaarsak", required = false)
    var sluttaarsak: Kodeverksentitet?,
    @JsonProperty(value = "sporingsinformasjon", required = true)
    var sporingsinformasjon: Sporingsinformasjon
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sporingsinformasjon(
    @JsonProperty(value = "opprettetTidspunkt", required = true)
    var opprettetTidspunkt: LocalDateTime,
    @JsonProperty(value = "endretTidspunkt", required = true)
    var endretTidspunkt: LocalDateTime,
    @JsonProperty(value = "opprettetAv", required = true)
    var opprettetAv: String,
    @JsonProperty(value = "opprettetKilde", required = true)
    var opprettetKilde: String,
    @JsonProperty(value = "endretAv", required = true)
    var endretAv: String,
    @JsonProperty(value = "endretKilde", required = true)
    var endretKilde: String,
)