package no.nav.aareg.teknisk_historikk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.aareg.teknisk_historikk.aareg_services.contract.Arbeidsforhold
import no.nav.aareg.teknisk_historikk.aareg_services.mapArbeidsforhold
import no.nav.aareg.teknisk_historikk.models.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

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
    fun hentArbeidsforholdForArbeidstaker(soekeparametere: Soekeparametere): FinnTekniskHistorikkForArbeidstaker200Response {
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
    }

    private fun createRequestEntity(soekeparametere: Soekeparametere): HttpEntity<JsonNode> {
        val headers = HttpHeaders().apply {
            setBearerAuth(
                azureTokenConsumer.getToken(listOf(aaregServicesConfig.scope))
            )
            contentType = MediaType.APPLICATION_JSON
            set("Nav-Personident", soekeparametere.arbeidstakerident)
            if (soekeparametere.arbeidsstedident != null) {
                set("Nav-Arbeidsstedident", soekeparametere.arbeidsstedident)
            }
            if (soekeparametere.opplysningspliktig != null) {
                set("Nav-Opplysningspliktigident", soekeparametere.opplysningspliktig)
            }
            //TODO filtrering på periode
        }
        return HttpEntity(headers)
    }

    companion object {
        val reader: ObjectReader = ObjectMapper().apply {
            this.registerModule(JavaTimeModule())
        }.readerForListOf(Arbeidsforhold::class.java)
    }
}


