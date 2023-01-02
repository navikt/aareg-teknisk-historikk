package no.nav.aareg.teknisk_historikk

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.aareg.teknisk_historikk.api.gyldigSoekeparameter
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
class MaskinportenTokenTest : AaregTekniskHistorikkTest() {
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
        WireMock.stubFor(
            WireMock.post("/token").willReturn(
                WireMock.okJson("{\"access_token\":\"testtoken\", \"expires_in\": 10}")
            )
        )

        WireMock.stubFor(
            WireMock.get("/wellknown").willReturn(
                WireMock.okJson("{\"token_endpoint\":\"${wmRuntimeInfo.httpBaseUrl}/token\"}")
            )
        )
    }

    @Test
    fun `kan lese konsument fra jwt-token`(wmRuntimeInfo: WireMockRuntimeInfo) {
        Mockito.`when`(jwtDecoder.decode(testToken)).thenReturn(testJwt)
        WireMock.stubFor(
            WireMock.get(AAREG_SERVICES_URI)
                .withHeader("Nav-Konsument", WireMock.equalTo(testorg))
                .willReturn(WireMock.okJson("[]"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            String::class.java
        )

        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    fun `kan lese databehandler fra jwt-tokenet`(wmRuntimeInfo: WireMockRuntimeInfo) {
        Mockito.`when`(jwtDecoder.decode(testToken)).thenReturn(testJwtMedDatabehandler)
        WireMock.stubFor(
            WireMock.get(AAREG_SERVICES_URI)
                .withHeader("Nav-Konsument", WireMock.equalTo(testorg))
                .withHeader("Nav-Databehandler", WireMock.equalTo(testsupplier))
                .willReturn(WireMock.okJson("[]"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            String::class.java
        )

        assertEquals(HttpStatus.OK, result.statusCode)
    }
}