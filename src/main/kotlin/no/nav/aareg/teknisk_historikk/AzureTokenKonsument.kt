package no.nav.aareg.teknisk_historikk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aareg.teknisk_historikk.models.TjenestefeilResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*
import javax.servlet.http.HttpServletRequest

@ConfigurationProperties(prefix = "azure.app")
data class AzureProperties(
    var clientId: String = "",
    var clientSecret: String = "",
    var wellKnownUrl: String = ""
)

@Component
@EnableConfigurationProperties(AzureProperties::class)
class AzureTokenConsumer(
    private val azureProperties: AzureProperties,
    private val restTemplate: RestTemplate
) {
    private var oidcConfiguration: OidcConfiguration? = null
    private var token: String? = null
    private var expiry: LocalDateTime? = null
    fun getToken(scopes: List<String>): String? {
        updateTokenIfNeeded(scopes)
        return token
    }

    @Synchronized
    private fun updateTokenIfNeeded(scopes: List<String>) {
        if (shouldRefresh(expiry)) {
            try {
                updateToken(scopes)
            } catch (e: RuntimeException) {
                throw AzureKonsumentException(Feilkode.AZURE_KONSUMENT_FEIL.toString(), e)
            }
        }
    }

    private fun shouldRefresh(expiry: LocalDateTime?) = expiry?.isBefore(now().plusMinutes(1)) ?: true

    private fun updateToken(scopes: List<String>) {
        if (oidcConfiguration == null) {
            oidcConfiguration = restTemplate.getForObject(azureProperties.wellKnownUrl, OidcConfiguration::class.java)
        }
        val responseBody = restTemplate.postForEntity(oidcConfiguration?.tokenEndpoint, HttpEntity(formParameters(scopes), headers()), AccessTokenResponse::class.java).body
        if (responseBody != null) {
            expiry = now().plusSeconds(responseBody.expiresIn)
            token = responseBody.accessToken
        }
    }

    private fun headers() = HttpHeaders().apply {
        accept = listOf(MediaType.APPLICATION_JSON)
        contentType = MediaType.APPLICATION_FORM_URLENCODED
        setBasicAuth(azureProperties.clientId, azureProperties.clientSecret)
    }

    private fun formParameters(scopes: List<String>) = LinkedMultiValueMap<String, String>().apply {
        add("grant_type", "client_credentials")
        add("scope", scopes.joinToString(" "))
    }
}

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AzureKonsumentFeilmeldinger {
    private val log: Logger = LoggerFactory.getLogger(AzureKonsumentFeilmeldinger::class.java)

    @ExceptionHandler(AzureKonsumentException::class)
    fun azurekonsumentFeil(exception: Throwable, httpServletRequest: HttpServletRequest): ResponseEntity<TjenestefeilResponse> {
        log.error("Feil ved henting av Azure AD-token", exception)
        return tjenestefeilRespons(
            httpServletRequest,
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.message!!
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private class AccessTokenResponse {
    @JsonProperty("expires_in")
    val expiresIn: Long = 0

    @JsonProperty("access_token")
    val accessToken: String? = null
}

@JsonIgnoreProperties(ignoreUnknown = true)
private class OidcConfiguration {
    @JsonProperty(value = "token_endpoint", required = true)
    val tokenEndpoint: String? = null
}

private class AzureKonsumentException(message: String, cause: Throwable) : Exception(message, cause)