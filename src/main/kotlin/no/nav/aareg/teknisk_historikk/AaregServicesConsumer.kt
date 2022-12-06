package no.nav.aareg.teknisk_historikk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.aareg.teknisk_historikk.contract.Arbeidsforhold
import no.nav.aareg.teknisk_historikk.models.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.lang.IllegalArgumentException
import java.time.ZoneOffset.UTC

@ConfigurationProperties(prefix = "app.aareg-services")
data class AaregServicesConfig(
    var url: String = "",
    var scope: String = ""
)

@Component
@EnableConfigurationProperties(AaregServicesConfig::class)
open class AaregServicesConsumer(
    private val aaregServicesConfig: AaregServicesConfig,
    private val azureTokenConsumer: AzureTokenConsumer,
    private val restTemplate: RestTemplate
) {
    fun hentArbeidsforholdForArbeidstaker(arbeidstakerIdent: String): FinnTekniskHistorikkForArbeidstaker200Response {
        val aaregServicesResponse: List<Arbeidsforhold> = restTemplate.exchange(
            "${aaregServicesConfig.url}/api/beta/tekniskhistorikk",
            HttpMethod.GET,
            createRequestEntity(arbeidstakerIdent), String::class.java
        ).body.let {
            reader.readValue(it)
        }

        return FinnTekniskHistorikkForArbeidstaker200Response().apply {
            antallArbeidsforhold = aaregServicesResponse.size
            arbeidsforholdForVirksomhet = aaregServicesResponse.groupBy(grupperArbeidsforhold)
                .map { gruppering ->
                    Arbeidsforholdgruppe().apply {
                        arbeidstaker = Person().apply {
                            offentligIdent = gruppering.key.arbeidstaker
                        }
                        if (gruppering.key.arbeidsstedErVirksomhet) {
                            arbeidssted = Underenhet().apply {
                                offentligIdent = gruppering.key.arbeidssted
                            }
                        } else {
                            arbeidssted = Person().apply {
                                offentligIdent = gruppering.key.arbeidssted
                            }
                        }
                        opplysningspliktig = Hovedenhet().apply {
                            type = "Hovedenhet"
                            offentligIdent = gruppering.key.opplysningspliktig
                        }
                        arbeidsforhold = gruppering.value.map { arbeidsforhold ->
                            Arbeidsforhold().apply {
                                id = arbeidsforhold.id
                                uuid = arbeidsforhold.navUuid
                                type = arbeidsforhold.type
                                rapporteringsordning = arbeidsforhold.rapporteringsordning
                                ansettelsesperioder = arbeidsforhold.ansettelsesperioder?.map { ansettelsesperiode ->
                                    Ansettelsesperiode().apply {
                                        startdato = ansettelsesperiode.startdato
                                        sluttdato = ansettelsesperiode.sluttdato
                                        sluttaarsak = ansettelsesperiode.sluttaarsak
                                        sporingsinformasjon = mapSporingsinformasjon(ansettelsesperiode.sporingsinformasjon, ansettelsesperiode.sluttdato != null)
                                    }
                                }
                                sporingsinformasjon = mapSporingsinformasjon(arbeidsforhold.sporingsinformasjon, arbeidsforhold.bruksperiode.tom != null)
                            }
                        }
                    }
                }
        }
    }

    private fun createRequestEntity(arbeidstakerIdent: String): HttpEntity<JsonNode> {
        val headers = HttpHeaders().apply {
            setBearerAuth(
                azureTokenConsumer.getToken(listOf(aaregServicesConfig.scope))
            )
            contentType = MediaType.APPLICATION_JSON
            set("Nav-Personident", arbeidstakerIdent)
        }
        return HttpEntity(headers)
    }

    companion object {
        val reader: ObjectReader = ObjectMapper().apply {
            this.registerModule(JavaTimeModule())
        }.readerForListOf(Arbeidsforhold::class.java)

        val mapSporingsinformasjon = { sporingsinformasjon: no.nav.aareg.teknisk_historikk.contract.Sporingsinformasjon, avsluttet: Boolean ->
            Sporingsinformasjon().apply {
                opprettetTidspunkt = sporingsinformasjon.opprettetTidspunkt.atOffset(UTC)
                opprettetAv = kalkulerEndringskilde(sporingsinformasjon.opprettetKilde)
                if (avsluttet) {
                    slettetTidspunkt = sporingsinformasjon.endretTidspunkt.atOffset(UTC)
                    slettetAv = kalkulerEndringskilde(sporingsinformasjon.endretKilde)
                }
            }
        }
        val grupperArbeidsforhold = { arbeidsforhold: Arbeidsforhold ->
            Gruppering(
                arbeidsforhold.arbeidstaker.identer.first { it.type == "FOLKEREGISTERIDENT" && it.gjeldende }.ident,
                arbeidsforhold.arbeidssted.let {
                    if (it.type == "Underenhet") {
                        it.identer.first().ident
                    } else {
                        it.identer.first { it.type == "FOLKEREGISTERIDENT" && it.gjeldende }.ident
                    }
                },
                arbeidsforhold.arbeidssted.type == "Underenhet",
                arbeidsforhold.opplysningspliktig.identer.first().ident
            )
        }
    }
}

fun kalkulerEndringskilde(kilde: String): Endringskilde {
    return when (kilde) {
        "EDAG" -> Endringskilde.A_ORDNINGEN
        "KONVERTERING" -> Endringskilde.FOER_A_ORDNINGEN
        "AAREG" -> Endringskilde.SAKSBEHANDLER
        "GJENOPPBYGG_IM" -> Endringskilde.GJENOPPBYGGING
        "AVSLUTT_AF", "AVSLUTNING" -> Endringskilde.MASKINELT_AVSLUTTET
        "DB_SKRIPT", "OPPRYDNING", "STRT_SLUTT_ANSPER", "TILPASSET" -> Endringskilde.PATCH
        else -> throw IllegalArgumentException("Ukjent kilde")
    }
}

data class Gruppering(
    val arbeidstaker: String,
    val arbeidssted: String,
    val arbeidsstedErVirksomhet: Boolean,
    val opplysningspliktig: String
)
