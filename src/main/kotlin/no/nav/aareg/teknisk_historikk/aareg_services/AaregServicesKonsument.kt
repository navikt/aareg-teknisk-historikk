package no.nav.aareg.teknisk_historikk.aareg_services

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.aareg.teknisk_historikk.*
import no.nav.aareg.teknisk_historikk.aareg_services.contract.Arbeidsforhold
import no.nav.aareg.teknisk_historikk.models.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import javax.servlet.http.HttpServletRequest

@ConfigurationProperties(prefix = "app.aareg.services")
data class AaregServicesConfig(
    var url: String = "",
    var scope: String = ""
)

@Component
@EnableConfigurationProperties(AaregServicesConfig::class)
class AaregServicesKonsument(
    private val aaregServicesConfig: AaregServicesConfig,
    private val azureTokenConsumer: AzureTokenConsumer,
    private val restTemplate: RestTemplate
) {
    fun hentArbeidsforholdForArbeidstaker(soekeparametere: Soekeparametere): List<Arbeidsforhold> {
        try {
            return restTemplate.exchange(
                "${aaregServicesConfig.url}/api/beta/tekniskhistorikk",
                HttpMethod.GET,
                createRequestEntity(soekeparametere), String::class.java
            ).body.let {
                reader.readValue(it)
            }
        } catch (notfound: NotFound) {
            throw notfound
        } catch (e: Forbidden) {
            throw AaregServicesForbiddenException(e)
        } catch (e: HttpStatusCodeException) {
            val feilkode = when (e) {
                is Unauthorized -> Feilkode.AAREG_SERVICES_UNAUTHORIZED
                else -> Feilkode.AAREG_SERVICES_ERROR
            }
            throw AaregServicesKonsumentException(feilkode.toString(), e)
        } catch (e: JsonMappingException) {
            throw AaregServicesKonsumentException(Feilkode.AAREG_SERVICES_MALFORMED.toString(), e)
        }
    }

    private fun createRequestEntity(soekeparametere: Soekeparametere): HttpEntity<JsonNode> {
        return HttpEntity(
            HttpHeaders().apply {
                setBearerAuth(
                    azureTokenConsumer.getToken(listOf(aaregServicesConfig.scope))
                )
                contentType = MediaType.APPLICATION_JSON
            }
                .medKonsumentOgDatabehandler()
                .medSoekeparametere(soekeparametere)
                .medKorrelasjonsid()
        )
    }

    private fun HttpHeaders.medKorrelasjonsid(): HttpHeaders {
        set(KORRELASJONSID_HEADER, MDC.get(KORRELASJONSID_HEADER))
        return this
    }
}

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AaregServicesKonsumentFeilmeldinger {
    private val log: Logger = LoggerFactory.getLogger(AaregServicesKonsumentFeilmeldinger::class.java)

    @ExceptionHandler(NotFound::class)
    fun ikkeFunnetFeil(ikkeFunnet: NotFound, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(
            httpServletRequest,
            HttpStatus.NOT_FOUND,
            ikkeFunnet.responseBodyAsString
        )

    @ExceptionHandler(AaregServicesForbiddenException::class)
    fun forbudtFeil(forbudt: Throwable, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(
            httpServletRequest,
            HttpStatus.FORBIDDEN,
            Feilkode.AAREG_SERVICES_FORBIDDEN.toString()
        )

    @ExceptionHandler(AaregServicesKonsumentException::class)
    fun aaregServicesKonsumentFeil(exception: Throwable, httpServletRequest: HttpServletRequest): ResponseEntity<TjenestefeilResponse> {
        log.error(exception.message!!, exception)
        return tjenestefeilRespons(
            httpServletRequest,
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.message!!
        )
    }
}

fun HttpHeaders.medSoekeparametere(soekeparametere: Soekeparametere) = HttpHeaders(this).apply {
    set("Nav-Personident", soekeparametere.arbeidstaker)
    if (soekeparametere.arbeidssted != null) {
        set("Nav-Arbeidsstedident", soekeparametere.arbeidssted)
    }
    if (soekeparametere.opplysningspliktig != null) {
        set("Nav-Opplysningspliktigident", soekeparametere.opplysningspliktig)
    }
}

private val reader: ObjectReader = ObjectMapper().apply {
    this.registerModule(JavaTimeModule())
}.readerForListOf(Arbeidsforhold::class.java)

private class AaregServicesForbiddenException(e: Forbidden) : Exception(e)

private class AaregServicesKonsumentException(message: String, cause: Throwable) : Exception(message, cause)