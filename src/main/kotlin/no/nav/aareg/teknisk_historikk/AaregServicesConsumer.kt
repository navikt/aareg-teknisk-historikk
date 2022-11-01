package no.nav.aareg.teknisk_historikk

import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import no.nav.aareg.teknisk_historikk.models.Soekeparametere
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@ConfigurationProperties(prefix = "app.aareg-services")
data class AaregServicesConfig(
    var url: String = ""
)

@Component
@EnableConfigurationProperties(AaregServicesConfig::class)
open class AaregServicesConsumer(
    private val aaregServicesConfig: AaregServicesConfig,
    private val azureTokenConsumer: AzureTokenConsumer,
    private val restTemplate: RestTemplate
) {
    fun hentArbeidsforholdForArbeidstaker(arbeidstakerIdent: String): FinnTekniskHistorikkForArbeidstaker200Response {
        val requestbody = Soekeparametere().apply {
            arbeidstakerident = arbeidstakerIdent
        }
        return restTemplate.postForObject("${aaregServicesConfig.url}/api/v1/arbeidsforhold", createRequestEntity(requestbody), FinnTekniskHistorikkForArbeidstaker200Response::class.java)
    }

    private fun createRequestEntity(soekeparametere: Soekeparametere): HttpEntity<Soekeparametere> {
        val headers = HttpHeaders().apply {
            setBearerAuth(
                azureTokenConsumer.getToken(listOf())
            )
            contentType = MediaType.APPLICATION_JSON
            set("Tema", "AAR")
        }
        return HttpEntity<Soekeparametere>(soekeparametere, headers)
    }
}
