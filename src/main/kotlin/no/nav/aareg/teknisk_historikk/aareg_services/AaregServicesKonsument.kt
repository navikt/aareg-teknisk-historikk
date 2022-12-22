package no.nav.aareg.teknisk_historikk.aareg_services

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.aareg.teknisk_historikk.*
import no.nav.aareg.teknisk_historikk.aareg_services.contract.Arbeidsforhold
import no.nav.aareg.teknisk_historikk.models.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.Forbidden
import org.springframework.web.client.HttpClientErrorException.Unauthorized
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@ConfigurationProperties(prefix = "app.aareg.services")
data class AaregServicesConfig(
    var url: String = "",
    var scope: String = ""
)

@Component
@EnableConfigurationProperties(AaregServicesConfig::class)
class AaregServicesConsumer(
    private val aaregServicesConfig: AaregServicesConfig,
    private val azureTokenConsumer: AzureTokenConsumer,
    private val restTemplate: RestTemplate,
    @Value("\${app.name}") private val appName: String
) {
    private val log = LoggerFactory.getLogger(this.javaClass.name)
    fun hentArbeidsforholdForArbeidstaker(soekeparametere: Soekeparametere): FinnTekniskHistorikkForArbeidstaker200Response {
        try {
            val aaregServicesResponse: List<Arbeidsforhold> = restTemplate.exchange(
                "${aaregServicesConfig.url}/api/beta/tekniskhistorikk",
                HttpMethod.GET,
                createRequestEntity(soekeparametere), String::class.java
            ).body.let {
                reader.readValue(it)
            }

            return FinnTekniskHistorikkForArbeidstaker200Response().apply {
                antallArbeidsforhold = aaregServicesResponse.size
                arbeidsforhold = aaregServicesResponse.map(mapArbeidsforhold)
            }
        } catch (e: Forbidden) {
            throw AaregServicesForbiddenException(e)
        } catch (e: HttpStatusCodeException) {
            val feilkode = when (e) {
                is Unauthorized -> Feilkode.AAREG_SERVICES_UNAUTHORIZED
                else -> Feilkode.AAREG_SERVICES_ERROR
            }
            val feil = Feil(feilkode, e)
            log.error(feilkode.toString(), feil)
            throw feil
        } catch (e: JsonMappingException) {
            val feil = Feil(Feilkode.AAREG_SERVICES_MALFORMED, e)
            log.error(Feilkode.AAREG_SERVICES_MALFORMED.toString(), e)
            throw feil
        }
    }

    private fun createRequestEntity(soekeparametere: Soekeparametere): HttpEntity<JsonNode> {
        val headers = HttpHeaders().apply {
            setBearerAuth(
                azureTokenConsumer.getToken(listOf(aaregServicesConfig.scope))
            )
            contentType = MediaType.APPLICATION_JSON
            set("Nav-Call-Id", appName)
            set(KORRELASJONSID_HEADER, MDC.get(KORRELASJONSID_HEADER))
            set("Nav-Personident", soekeparametere.arbeidstaker)
            if (soekeparametere.arbeidssted != null) {
                set("Nav-Arbeidsstedident", soekeparametere.arbeidssted)
            }
            if (soekeparametere.opplysningspliktig != null) {
                set("Nav-Opplysningspliktigident", soekeparametere.opplysningspliktig)
            }
            val orgNr = hentOrgnrFraToken()
            set("Nav-Konsument", orgNr.konsument)
            if (orgNr.databehandler != null) set("Nav-Databehandler", orgNr.databehandler)
        }
        return HttpEntity(headers)
    }

    companion object {
        val reader: ObjectReader = ObjectMapper().apply {
            this.registerModule(JavaTimeModule())
        }.readerForListOf(Arbeidsforhold::class.java)
    }
}

class AaregServicesForbiddenException(e: Forbidden) : Exception(e)