package no.nav.aareg.teknisk_historikk

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import no.nav.aareg.teknisk_historikk.models.Soekeparametere
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus

@WireMockTest(httpPort = WIREMOCK_PORT)
class AzureTokenKonsumentTest : AaregTekniskHistorikkTest() {
    lateinit var logWatcher: ListAppender<ILoggingEvent>

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    companion object {
        val ENDEPUNKT_URI = "/api/v1/arbeidsforhold"
        val AAREG_SERVICES_URI = "/api/beta/tekniskhistorikk"

        @JvmStatic
        @BeforeAll
        internal fun setup(wmRuntimeInfo: WireMockRuntimeInfo) {
            System.setProperty("wiremock.server.baseUrl", wmRuntimeInfo.httpBaseUrl)
        }
    }

    @BeforeEach
    fun setup(wmRuntimeInfo: WireMockRuntimeInfo) {
        logWatcher = ListAppender<ILoggingEvent>().apply { this.start() }

        stubFor(
            get("/wellknown").willReturn(
                okJson("{\"token_endpoint\":\"${wmRuntimeInfo.httpBaseUrl}/token\"}")
            )
        )
    }

    @Test
    fun `vellykket henting av ad-token`(wmRuntimeInfo: WireMockRuntimeInfo) {
        stubFor(
            post("/token").willReturn(
                okJson("{\"access_token\":\"testtoken\", \"expires_in\": 10000}")
            )
        )
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .withHeader("Nav-Personident", equalTo("123456"))
                .willReturn(okJson("[]"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(Soekeparametere().apply { arbeidstakerident = "123456" }),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        assertEquals(HttpStatus.OK, result.statusCode)
    }
}