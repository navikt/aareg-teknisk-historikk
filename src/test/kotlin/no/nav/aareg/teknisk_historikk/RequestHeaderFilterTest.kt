package no.nav.aareg.teknisk_historikk

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.aareg.teknisk_historikk.api.gyldigSoekeparameter
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus

@WireMockTest(httpPort = WIREMOCK_PORT)
class RequestHeaderFilterTest : AaregTekniskHistorikkTest() {
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
        WireMock.stubFor(
            WireMock.post("/token").willReturn(
                WireMock.okJson("{\"access_token\":\"$testAzureToken\", \"expires_in\": 10}")
            )
        )

        WireMock.stubFor(
            WireMock.get("/wellknown").willReturn(
                WireMock.okJson("{\"token_endpoint\":\"${wmRuntimeInfo.httpBaseUrl}/token\"}")
            )
        )
    }

    @Test
    fun `aareg-services mottar korrelasjons-id`(wmRuntimeInfo: WireMockRuntimeInfo) {
        WireMock.stubFor(
            WireMock.get(AAREG_SERVICES_URI)
                .withHeader("Nav-Call-Id", WireMock.equalTo("aareg-teknisk-historikk-test"))
                .willReturn(WireMock.okJson("[]"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(0, result.body?.antallArbeidsforhold)
    }

    @Test
    fun `aareg-services mottar call-id`(wmRuntimeInfo: WireMockRuntimeInfo) {
        WireMock.stubFor(
            WireMock.get(AAREG_SERVICES_URI)
                .withHeader(KORRELASJONSID_HEADER, WireMock.equalTo("test-korrelasjons-id"))
                .willReturn(WireMock.okJson("[]"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering().medKorrelasjonsid("test-korrelasjons-id")),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(0, result.body?.antallArbeidsforhold)
    }
}