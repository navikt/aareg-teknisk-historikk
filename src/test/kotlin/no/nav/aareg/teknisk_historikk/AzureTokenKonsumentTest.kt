package no.nav.aareg.teknisk_historikk

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.aareg.teknisk_historikk.api.gyldigSoekeparameter
import no.nav.aareg.teknisk_historikk.api.tjenestefeilMedMelding
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import no.nav.aareg.teknisk_historikk.models.TjenestefeilResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.LoggerFactory
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
        Mockito.`when`(jwtDecoder.decode(testToken)).thenReturn(testJwt)

        stubFor(
            get("/wellknown").willReturn(
                okJson("{\"token_endpoint\":\"${wmRuntimeInfo.httpBaseUrl}/token\"}")
            )
        )
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer $testAzureToken"))
                .willReturn(okJson("[]"))
        )
    }

    @Test
    fun `vellykket henting av ad-token`(wmRuntimeInfo: WireMockRuntimeInfo) {
        stubFor(
            post("/token").willReturn(
                okJson("{\"access_token\":\"$testAzureToken\", \"expires_in\": 10}")
            )
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    fun `henting av ad-token feilet`(wmRuntimeInfo: WireMockRuntimeInfo) {
        (LoggerFactory.getLogger(AzureKonsumentFeilmeldinger::class.java) as Logger).addAppender(logWatcher)
        stubFor(
            post("/token").willReturn(
                serverError()
            )
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentiseringOgKorrelasjon()),
            TjenestefeilResponse::class.java
        )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        assertEquals(tjenestefeilMedMelding(Feilkode.AZURE_KONSUMENT_FEIL.toString()), result.body)
        assertEquals("Feil ved henting av Azure AD-token", logWatcher.list.first().message)
    }
}

const val testAzureToken = "testazuretoken"