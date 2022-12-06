package no.nav.aareg.teknisk_historikk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*
import java.util.logging.Logger

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
    private val log = Logger.getLogger(this::javaClass.name)

    private var oidcConfiguration: OidcConfiguration? = null
    private var token: String? = null
    private var expiry: LocalDateTime? = null
    fun getToken(scopes: List<String?>): String? {
        updateTokenIfNeeded(scopes)
        return token
    }

    @Synchronized
    private fun updateTokenIfNeeded(scopes: List<String?>) {
        if (shouldRefresh(expiry)) {
            try {
                updateToken(scopes)
            } catch (e: RuntimeException) {
                log.info("Feil fanget: ${e.message}")
                if (hasExpired(expiry)) {
                    throw TokenExpiredException("En feil oppsto ved forsøk på å refreshe AzureAD token", e)
                }
            }
        }
    }

    private fun hasExpired(expiry: LocalDateTime?) = expiry?.isBefore(now()) ?: true

    private fun shouldRefresh(expiry: LocalDateTime?) = expiry?.isBefore(now().plusMinutes(1)) ?: true

    private fun updateToken(scopes: List<String?>) {
        if (oidcConfiguration == null) {
            oidcConfiguration = restTemplate.getForObject(azureProperties.wellKnownUrl, OidcConfiguration::class.java)
        }
        val responseBody = restTemplate.postForEntity(oidcConfiguration?.tokenEndpoint, HttpEntity(formParameters(scopes), headers()), AccessTokenResponse::class.java).body
        if (responseBody != null) {
            expiry = now().plusSeconds(responseBody.expiresIn)
            token = responseBody.accessToken
        }
    }

    private fun headers(): HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.setBasicAuth(azureProperties.clientId, azureProperties.clientSecret)
        return headers
    }

    private fun formParameters(scopes: List<String?>): MultiValueMap<String, String> {
        val formParameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        formParameters.add("grant_type", "client_credentials")
        formParameters.add("scope", scopes.joinToString(" "))
        return formParameters
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

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
private class TokenExpiredException(message: String, cause: Throwable) : RuntimeException(message, cause)
